package edu.usc.sql.analyses.layoutanalysis;

import edu.usc.sql.AndroidApp;
import edu.usc.sql.callgraph.CustomCallGraph;
import edu.usc.sql.callgraph.Node;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import soot.*;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.tagkit.IntegerConstantValueTag;
import soot.util.Chain;

import java.io.*;
import java.util.*;

/**
 * Created by mian on 8/11/16.
 */
public class AnalyzeActivityUI {

    private static final String WIDTH_TAG = "layout_width";
    private static final String HEIGHT_TAG = "layout_height";


    public static void main(String[] args) {
        String pythonPath = "/Users/mian/Desktop/SizeModel/modeling.py";
        String apkPath = "/Users/mian/Documents/Input/appinventor.ai_progetto2003.SCAN_2.5.24";
        String frameworkPath = "/Users/mian/Documents/Projects/framework-res";
        String mainAct = findMainActivity(pythonPath, apkPath);

        args = new String[]{"/Users/mian/Documents/Input/SootAppSet",
                "/Users/mian/Documents/Input/SootAppSet/appinventor.ai_progetto2003.SCAN_2.5.24.apk",
                "/Users/mian/Documents/Input/SootAppSet/appinventor.ai_progetto2003.SCAN_2.5.24.txt"};
        if (args.length != 3) {
            System.out.println("Usage: android_jar_path apk_path class_list_path");
            return;
        }

        if (mainAct != null) {
            AndroidApp app = new AndroidApp(args[0], args[1], args[2]);
            String layout = "activity_start";//findLayoutXml(app, mainAct);
            String layoutXmlPath = apkPath + "/res/layout/" + layout + ".xml";
            WidgetNode rootNode = createLayoutTree(layoutXmlPath);
            /*
            Customized for laclaveganadora.miconversor_2.6
             */
            /*WidgetNode newRoot = createLayoutTree(apkPath + "/res/layout/list_item.xml");
            addSubTree(rootNode, "@id/spinnerfrom", newRoot);
            WidgetNode secondRoot = createLayoutTree(apkPath + "/res/layout/list_item.xml");
            addSubTree(rootNode, "@id/spinnerto", secondRoot);*/

            Constants.parseXMLConstant(apkPath, frameworkPath);
            breadthFirstTraverse(apkPath, rootNode);
//            depthFirstDumpWidget(rootNode);
            preOrderTraverse(rootNode, null);
            postOrderTraverse(rootNode, apkPath);
            Map<String, Integer> colorToSize = calculateDisplayedSize(rootNode);
            breadthFirstDumpWidget(rootNode);
            System.out.println("Primary color:" + getPrimaryColor(colorToSize));
        }


    }

    /**
     * Call the python script to find the name of the main Activity
     * @param pyPath
     * @param apkPath
     * @return
     */
    public static String findMainActivity(String pyPath, String apkPath) {
        String main = "";
        try {
            Process p = Runtime.getRuntime().exec("python " + pyPath + " -a " + apkPath);
            p.waitFor();
            if (p.exitValue() == 0) {
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;

                while((line = br.readLine()) != null) {
                    main += line;
                }
                if (occurTimes(main, "\n") != 0)
                    System.err.println("You may run the script having multiple lines of output!");
                System.out.println(main);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while((line = br.readLine()) != null) {
                    System.err.println(line);
                }
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            return main;
        }
    }

    /**
     * Search the invocation statement in the Activity class,
     * locate the xml name from R.layout class
     * @param app
     * @param activityClass
     * @return
     */
    public static String findLayoutXml(AndroidApp app, String activityClass) {
        Set<SootClass> allClasses = app.getAllClasses();
        String arg = "";
        String layoutFileName = "";

        for (SootClass sc : allClasses) {
            if(sc.getName().equals(activityClass)) {
                CustomCallGraph callGraph = app.getCallGraph();
                SootMethod onCreate = sc.getMethod("void onCreate(android.os.Bundle)");
                InvokeStmt targetInvoke = searchInvocation(onCreate, callGraph, "void setContentView(int)");
                if (targetInvoke != null) {
                    Value v = targetInvoke.getInvokeExpr().getArg(0);
                    if (v instanceof Constant) {
                        arg = v.toString();
                    } else {
                        System.err.println("Layout ID is not a constant, need further processing!");
                    }
                } else {
                    System.err.println("Cannot locate setContentView(int)!");
                }
                break;
            }
        }
        // R$layout may be more than one
        Set<SootClass> layoutSet = new HashSet<SootClass>();
        for (SootClass sc : allClasses) {
            if (sc.getName().contains("R$layout")) {
                layoutSet.add(sc);

            }
        }
        for (SootClass sc : layoutSet) {
            Chain<SootField> fields = sc.getFields();
            for (SootField sf : fields) {
                Type fieldType = sf.getType();
                if (fieldType instanceof IntType && sf.hasTag("IntegerConstantValueTag")) {
                    IntegerConstantValueTag tag = (IntegerConstantValueTag) sf.getTag("IntegerConstantValueTag");
                    if (tag != null) {
                        int value = tag.getIntValue();
                        if (arg != "" && value == Integer.parseInt(arg)) {
                            layoutFileName = sf.getName();
                            break;
                        }
                    } else {
                        System.err.println("Cannot get the value of " + sf.getSignature());
                    }
                }

            }
        }

        return layoutFileName;
    }

    /**
     * Check all the callees of sm, and see which callee invoke targetMethod
     * @param sm
     * @param cg
     * @param targetMethod
     * @return the invoke statement of the target method
     */
    public static InvokeStmt searchInvocation(SootMethod sm, CustomCallGraph cg, String targetMethod) {
        InvokeStmt target = null;
        Queue<Node> queue = new LinkedList<Node>();
        for (Node node : cg.getHeads()) {
            if (node.getMethod().equals(sm)) {
                queue.add(node);
                break;
            }
        }

        while (!queue.isEmpty()) {
            Node curr = queue.remove();
            Body body = curr.getMethod().retrieveActiveBody();
            Chain<Unit> unitChain = body.getUnits();
            Iterator<Unit> unitIt = unitChain.iterator();

            while (unitIt.hasNext()) {
                Stmt stmt = (Stmt) unitIt.next();
                if (stmt instanceof InvokeStmt) {
                    InvokeExpr expr = stmt.getInvokeExpr();
                    if (expr.getMethod().getSubSignature().equals(targetMethod)) {
                        target = (InvokeStmt) stmt;
                        break;
                    }
                }
            }
            if (target == null) {
                queue.addAll(curr.getChildren());
            } else {
                break;
            }
        }
        return target;
    }

    /*public static void getEstimatedSize(String pyPath, String layoutXml) {
        String result = "";
        try {
            Process p = Runtime.getRuntime().exec("python " + pyPath + " -m " + layoutXml);
            p.waitFor();
            if (p.exitValue() == 0) {
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;

                while((line = br.readLine()) != null) {
                    result += line;
                }

                System.out.println(result);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while((line = br.readLine()) != null) {
                    System.err.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * Create the Layout tree for the layout xml file
     * @param layoutXmlPath
     * @return
     */
    public static WidgetNode createLayoutTree(String layoutXmlPath) {
        SAXReader reader = new SAXReader();
        File file = new File(layoutXmlPath);
        WidgetNode rootNode = null;
        try {
            Document document = reader.read(file);
            Element root = document.getRootElement();
            Map<Element, WidgetNode> map = new HashMap<Element, WidgetNode>();
            Queue<Element> queue = new LinkedList<Element>();
            queue.add(root);
            boolean first = true;
            while(!queue.isEmpty()) {
                Element e = queue.poll();
                WidgetNode node;
                if (!map.containsKey(e)) {
                    if (first) {
                        first = false;
                        node = new WidgetNode(e, true);
                    } else {
                        node = new WidgetNode(e, false);
                    }
                    map.put(e, node);
                } else {
                    node = map.get(e);
                }

                for (Element child : e.elements()) {
                    queue.add(child);

                    WidgetNode childNode;
                    if (map.containsKey(child)) {
                        childNode = map.get(child);
                    } else {
                        childNode = new WidgetNode(child, false);
                        map.put(child, childNode);
                    }
                    node.addChild(childNode);
                }
            }

            rootNode = map.get(root);

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            return rootNode;
        }
    }

    public static void addSubTree(WidgetNode root, String id, WidgetNode subRoot) {
        Queue<WidgetNode> queue = new LinkedList<WidgetNode>();
        queue.add(root);
        while(!queue.isEmpty()) {
            WidgetNode curr = queue.poll();
            Element e = curr.getXmlElement();

            if (e.attributeValue("id") != null) {
                if (e.attributeValue("id").equals(id)){
                    curr.addChild(subRoot);
                    break;
                }
            }
            queue.addAll(curr.getChildren());
        }
    }

    /**
     * Using pre-order to handle "match_parent"
     * @param root
     * @param parent
     */
    public static void preOrderTraverse(WidgetNode root, WidgetNode parent){
        if (root == null)
            return;
        Element e = root.getXmlElement();
        System.out.println(e.attributeValue(WIDTH_TAG));
        if (e.attributeValue(WIDTH_TAG).equals("match_parent") || e.attributeValue(WIDTH_TAG).equals("fill_parent")) {
            if (parent == null) {
                root.setWidth(SizeModel.getScreenWidth());
            } else {
                if (parent.getXmlElement().attributeValue(WIDTH_TAG).equals("match_parent") ||
                        parent.getXmlElement().attributeValue(WIDTH_TAG).equals("fill_parent")) {
                    // Not consider padding yet
                    root.setWidth(parent.getWidth());
                } else if (parent.getXmlElement().attributeValue(WIDTH_TAG).equals("wrap_content")) {
                    e.addAttribute(WIDTH_TAG, "wrap_content");
                } else {
                    System.out.println("Parent is exact size, not handle yet!");
                }
            }

        }

        if (e.attributeValue(HEIGHT_TAG).equals("match_parent") || e.attributeValue(HEIGHT_TAG).equals("fill_parent")) {
            if (parent == null) {
                root.setHeight(SizeModel.getScreenHeight());
            } else {
                if (parent.getXmlElement().attributeValue(HEIGHT_TAG).equals("match_parent") ||
                        parent.getXmlElement().attributeValue(HEIGHT_TAG).equals("fill_parent")) {
                    root.setHeight(parent.getHeight());
                } else if (parent.getXmlElement().attributeValue(HEIGHT_TAG).equals("wrap_content")) {
                    e.addAttribute(HEIGHT_TAG, "wrap_content");
                } else {
                    System.out.println("Parent is exact size, not handle yet!");
                }
            }
        }

        for (WidgetNode child : root.getChildren()) {
            preOrderTraverse(child, root);
        }
     }

    /**
     * Breadth First Dump the tree
     * @param root
     */
     public static void breadthFirstDumpWidget(WidgetNode root) {
         Queue<WidgetNode>  queue = new LinkedList<WidgetNode>();
         queue.add(root);

         System.out.println("+++++++++++++++++++");
         int counter = 0;
         while (!queue.isEmpty()) {
             WidgetNode curr = queue.poll();
             System.out.println(++counter + "\t" +curr.getName() + "\t" + curr.getWidth() + "\t" + curr.getHeight()
                     + "\t" + curr.getDisplayedSize());
             queue.addAll(curr.getChildren());
         }
     }

     public static void depthFirstDumpWidget(WidgetNode root) {
         LinkedList<WidgetNode> linked = new LinkedList<WidgetNode>();
         linked.add(root);

         System.out.println("====================");
         int counter = 0;
         while (!linked.isEmpty()) {
             WidgetNode curr = linked.poll();
             System.out.println(++counter + "\t" +curr.getName() + "\t" + curr.getWidth() + "\t" + curr.getHeight()
                     + "\t" + curr.getDisplayedSize());
             for (WidgetNode child : curr.getChildren()) {
                 linked.addFirst(child);
             }
         }
     }

    public static void breadthFirstTraverse(String apkRootPath, WidgetNode root) {
        Queue<WidgetNode>  queue = new LinkedList<WidgetNode>();
        queue.add(root);

        while (!queue.isEmpty()) {
            WidgetNode curr = queue.poll();
            Element e = curr.getXmlElement();
            if (e.attributeValue("style") != null) {
                Constants.applyStyleToWidget(apkRootPath, curr);
            }
            queue.addAll(curr.getChildren());
        }
    }


    public static void postOrderTraverse(WidgetNode root, String apkPath) {
        if (root == null)
            return;

        for (WidgetNode child : root.getChildren()) {
            postOrderTraverse(child, apkPath);
        }

        modelSize(root, apkPath);
//        System.out.println(root.getName());
    }

    /**
     * Find the path of the drawable
     * @param apkPath the root dir of the unzipped apk
     * @param drawable
     * @return
     */
    public static String findDrawable(String apkPath, String drawable) {
        String imagePath = null;
        try {
            Process p = Runtime.getRuntime().exec("find " + apkPath + " -name " + drawable + ".*");
            p.waitFor();
            if (p.exitValue() == 0) {
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;

                while((line = br.readLine()) != null) {
                    // Consider the standard one
                    if (line.contains("drawable/")) {
                        imagePath = line;
                    }
                }

            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while((line = br.readLine()) != null) {
                    System.err.println(line);
                }
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            return imagePath;
        }
    }

    public static void modelSize(WidgetNode w, String apkPath) {

        Element e = w.getXmlElement();
        if (e.attributeValue(WIDTH_TAG).equals("wrap_content")) {
            String textSize;
            switch (e.getName()) {
                //
                case "ImageView":
                    String srcImage = e.attributeValue("src");
                    srcImage = Constants.getRealValue(srcImage);
                    String imagePath = findDrawable(apkPath, srcImage);

                    // Handle PNG image
                    if (imagePath.endsWith("png")) {
                        int imageWidth = SizeModel.getWidthBasedonImage(imagePath);
//                        System.out.println("ImageView width:" + imageWidth);
                        if (imageWidth != 0)
                            w.setWidth(imageWidth);
                        else
                            System.err.println("The width of the drawable cannot be 0!");
                    } else {
                        // Handle XML
                        System.out.println("Need further processing for ImageView: ");
                        System.out.println(imagePath);
                    }
                    break;
                case "EditText":
                    if (e.attributeValue("textSize") != null) {
                        textSize = Constants.getRealValue(e.attributeValue("textSize"));
                    } else {
                        // Default text size
                        textSize = "18sp";
                    }

                    /*
                     Save the text size for height calculation
                      */
                    w.setTextSize(textSize);

                    if (e.attributeValue("ems")  != null) {
                        w.setWidth(SizeModel.getWidthBasedonEm(textSize, Integer.parseInt(e.attributeValue("ems"))));
                    } else {
                        if (e.attributeValue("text") != null && e.attributeValue("text") != "") {
                            int textLength = Constants.getRealValue(e.attributeValue("text")).length();
                            int txtWidth = SizeModel.getWidthBasedonText(textSize, textLength);
                            w.setWidth(txtWidth);
                        } else if (e.attributeValue("hint") != null && e.attributeValue("text") != "") {
                            int textLength = Constants.getRealValue(e.attributeValue("hint")).length();
                            int hintWidth = SizeModel.getWidthBasedonText(textSize, textLength);
                            w.setWidth(hintWidth);
                        } else {
                            System.out.println("Cannot know the text for the EditText");
                        }
                    }
                    break;
                case "TextView":
                    if (e.attributeValue("textSize") != null) {
                        textSize = Constants.getRealValue(e.attributeValue("textSize"));
                    } else {
                        // Default text size
                        textSize = "14sp";
                    }

                    /*
                     Save the text size for height calculation
                      */
                    w.setTextSize(textSize);

                    if (e.attributeValue("ems")  != null) {
                        w.setWidth(SizeModel.getWidthBasedonEm(textSize, Integer.parseInt(e.attributeValue("ems"))));
                    } else {
                        int textLength = 0;
                        if (e.attributeValue("text") != null && e.attributeValue("text") != "") {
                            textLength = Constants.getRealValue(e.attributeValue("text")).length();
                        } else {
                            System.out.println("Cannot know the text for the TextView!");
                            if (e.attributeValue("id").equals("@id/result")) {
                                textLength = 15;
                            } else if(e.attributeValue("id").equals("@id/textUpdate")) {
                                textLength = 32;
                            } else if (e.attributeValue("id").equals("@id/title")) {
                                textLength = 23;
                            }
                            System.out.println("We assign some the length!");
                        }
                        int txtWidth = SizeModel.getWidthBasedonText(textSize, textLength);
                        w.setWidth(txtWidth);
                    }

                    break;
//                case "Spinner":
//                    // 1) ArrayAdapter
//                    // 2) SimpleAdapter
//                    // 3) SimpleAdapter
//                    // 4) BaseAdapter
//                    // Based on the constructor to find the item's layout
//                    int spinnerWidth = SizeModel.getWidthBasedonUnit(Constants.getRealValue("@dimen/size_flag_spinner"));
//                    spinnerWidth += SizeModel.getWidthBasedonText(Constants.getRealValue("@dimen/textsize_spinner"), 23);
//                    w.setWidth(spinnerWidth);
//                    break;
                case "Button":
                    if (e.attributeValue("background") != null) {
                        String bgImage = Constants.getRealValue(e.attributeValue("background"));
                        String bgImagePath = findDrawable(apkPath, bgImage);
                        // Handle PNG image
                        // Similar to ImageView
                        if (bgImagePath.endsWith("png")) {
                            // Handle PNG image
                            int imageWidth = SizeModel.getWidthBasedonImage(bgImagePath);
                            if (imageWidth != 0)
                                w.setWidth(SizeModel.getButtonWidth(imageWidth));
                            else
                                System.err.println("The width of the drawable cannot be 0!");
                        } else {
                            // Handle XML
                            if(e.attributeValue("text") != null) {
                                int textLength = Constants.getRealValue(e.attributeValue("text")).length();

                                if (e.attributeValue("textSize") != null) {
                                    textSize = Constants.getRealValue(e.attributeValue("textSize"));
                                } else {
                                    textSize = "14sp";
                                }
                                // Store the text size
                                w.setTextSize(textSize);
                                int textWidth = SizeModel.getWidthBasedonText(textSize, textLength);
                                w.setWidth(SizeModel.getButtonWidth(textWidth));
                            }
                        }
                    }

                    break;
                case "com.google.android.gms.ads.AdView":
                    w.setWidth(1080);
                    break;
                default:
                    int parentWidth = 0;
                    for (WidgetNode ch : w.getChildren()) {
                        parentWidth += ch.getWidth();
                    }
                    w.setWidth(parentWidth);
                    break;
            }

        } else if (!e.attributeValue(WIDTH_TAG).equals("match_parent") && !e.attributeValue(WIDTH_TAG).equals("fill_parent")) {
            String widthSetting = Constants.getRealValue(e.attributeValue(WIDTH_TAG));
            w.setWidth(SizeModel.getWidthBasedonUnit(widthSetting));
        }
        //*************************************
        // Model the height of each widget
        //*************************************
        if (e.attributeValue(HEIGHT_TAG).equals("wrap_content")) {
            switch (e.getName()) {
                //
                case "ImageView":
                    String srcImage = Constants.getRealValue(e.attributeValue("src"));
                    String imagePath = findDrawable(apkPath, srcImage);

                    // Handle PNG image
                    if (imagePath.endsWith("png")) {
                        int imageHeight = SizeModel.getHeightBasedonImage(imagePath);
                        if (imageHeight != 0)
                            w.setHeight(imageHeight);
                        else
                            System.err.println("The height of the drawable cannot be 0!");
                    } else {
                        // Handle XML
                        System.out.println("Need further processing for ImageView: ");
                        System.out.println(imagePath);
                    }
                    break;
                case "EditText":
                case "TextView":
                    String textSize;
                    if (w.getTextSize() == null) {
                        textSize = "14sp";
                    } else {
                        textSize = w.getTextSize();
                    }
                    int height = SizeModel.getHeightBaseonText(textSize);
                    int lines = 1;
                    if (e.attributeValue("lines") != null && e.attributeValue("lines") != "") {
                        lines = Integer.parseInt(e.attributeValue("lines"));
                    }
                    w.setHeight(height * lines);
                    break;
//                case "Spinner":
//                    w.setHeight(SizeModel.getWidthBasedonUnit(Constants.getRealValue("@dimen/size_flag_spinner")));
//                    break;
                case "Button":

                    if (w.getHeight() == 0) {
                        int btnHeight = SizeModel.getButtonHeight(SizeModel.getHeightBaseonText(w.getTextSize()));
                        w.setHeight(btnHeight);
                    }
                    // Already stored the height of the background image, do nothing
                    break;
                case "com.google.android.gms.ads.AdView":
                    w.setHeight(150);
                    break;
                default:
                    int parentHeight = 0;
                    for (WidgetNode ch : w.getChildren()) {
                        parentHeight = Math.max(ch.getHeight(), parentHeight);
                    }
                    w.setHeight(parentHeight);
                    break;
            }

        } else if (!e.attributeValue(WIDTH_TAG).equals("match_parent") && !e.attributeValue(WIDTH_TAG).equals("fill_parent")) {
            String heightSetting = Constants.getRealValue(e.attributeValue(HEIGHT_TAG));
            w.setHeight(SizeModel.getWidthBasedonUnit(heightSetting));
        }

//        System.out.println(w.getName() + ":" + w.getWidth() +"," + w.getHeight());

    }

    public static Map<String, Integer> calculateDisplayedSize(WidgetNode root) {
        Queue<WidgetNode>  queue = new LinkedList<WidgetNode>();
        queue.add(root);

        Map<String, Integer> colorToSize = new HashMap<String, Integer>();
        Set<String> backgroundExceptionSet = new HashSet<String>();
        backgroundExceptionSet.add("ImageView");
        while (!queue.isEmpty()) {
            WidgetNode curr = queue.poll();
            int remainingSize = curr.getWidth() * curr.getHeight();
            for (WidgetNode child : curr.getChildren()) {
                remainingSize -= child.getWidth() * child.getHeight();
                queue.add(child);
            }
            curr.setDisplayedSize(remainingSize);

            Element e = curr.getXmlElement();
            String widget = e.getName();
            String background;
            if (widget.equals("ImageView")) {
                background = e.attributeValue("src");
            } else {
                background = e.attributeValue("background");
                if (background == null) {
                    switch (widget) {
                        case "TextView":
                            background = "TextView";
                            break;
                        case "Button":
                        case "ImageButton":
                            background = "btn_default_material";
                            break;
                        case "EditText":
                            background = "edit_text_material";
                            break;
                        case "LinearLayout":
                        case "RelativeLayout":
                        case "FrameLayout":
                            background = "Window";
                            break;
                        default:
                            System.out.println("Missing the default background value for " + widget);
                            break;
                    }
                } else {
                    background = Constants.getRealValue(background);
                }

                if (colorToSize.containsKey(background)) {
                    int size = colorToSize.get(background);
                    size += remainingSize;
                    colorToSize.put(background, size);
                } else {
                    colorToSize.put(background, remainingSize);
                }
            }

        }
        return colorToSize;
    }

    public static String getPrimaryColor(Map<String, Integer> colorToSize) {
        List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(colorToSize.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            // Descending order
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        for (Map.Entry<String, Integer> entry : entryList) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }

        return entryList.get(0).getKey();
    }

    public static int occurTimes(String string, String a) {
        int pos = -2;
        int n = 0;

        while (pos != -1) {
            if (pos == -2) {
                pos = -1;
            }
            pos = string.indexOf(a, pos + 1);
            if (pos != -1) {
                n++;
            }
        }
        return n;
    }
}
