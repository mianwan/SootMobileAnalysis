package edu.usc.sql.analyses.layoutanalysis;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mian on 8/17/16.
 */
public class Constants {
    private static Map<String, String> dimenMap = new HashMap<String, String>();
    private static Map<String, String> stringMap = new HashMap<String, String>();
    private static Map<String, String> colorMap = new HashMap<String, String>();
    private static Map<String, String> sdkColorMap = new HashMap<String, String>();
    private static final String DIMEN = "dimen";
    private static final String ITEM = "item";
    private static final String ANDROID = "android:";

    public static void parseXMLConstant(String apkRootPath, String frameworkPath) {
        String dimenPath = apkRootPath + "/res/values/dimens.xml";
        String stringPath = apkRootPath + "/res/values/strings.xml";
        String colorPath = apkRootPath + "/res/values/colors.xml";
        String sdkColorPath = frameworkPath + "/res/values/colors.xml";
        parseXMLtoMap(dimenPath, dimenMap);
        parseXMLtoMap(stringPath, stringMap);
        parseXMLtoMap(sdkColorPath, sdkColorMap);


    }

    public static void parseXMLtoMap(String path, Map<String, String> map) {
        File file = new File(path);
        SAXReader reader = new SAXReader();
        if (file.exists() && !file.isDirectory()) {
            Document document = null;
            try {
                document = reader.read(file);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            Element root = document.getRootElement();
            for (Element child : root.elements()) {
                String text = child.getText();
                if (text.startsWith("@")) {
                    text = text.substring(text.indexOf("/")+ 1);
                    if (map.containsKey(text)) {
                        text = map.get(text);
                    }
                }
                map.put(child.attributeValue("name"), text);
            }
        }

    }

    public static void applyStyleToWidget(String apkRootPath, WidgetNode widgetNode) {
        File file = new File(apkRootPath + "/res/values/styles.xml");
        SAXReader reader = new SAXReader();
        if (file.exists() && !file.isDirectory()) {
            Document document = null;
            try {
                document = reader.read(file);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            String styleName = widgetNode.getXmlElement().attributeValue("style");
            styleName = styleName.substring(styleName.indexOf("/") + 1);

            Element targetStyle = null;
            for (Element e : document.getRootElement().elements()) {
                if (e.attributeValue("name").equals(styleName)) {
                    targetStyle = e;
                    break;
                }
            }
            // Not consider inheritance
            for (Element ch : targetStyle.elements()) {
                widgetNode.getXmlElement().addAttribute(ch.attributeValue("name").replace(ANDROID, ""), ch.getText());
            }
        }
    }

    public static Map<String, String> getDimenMap() {
        return dimenMap;
    }

    public static Map<String, String> getStringMap() {
        return stringMap;
    }

    public static String getRealValue(String value) {
        String realValue;
        if (value.contains("/")) {
            String key = value.substring(value.indexOf("/") + 1);
            if (value.startsWith("@string")) {
                realValue = stringMap.get(key);
            } else if (value.startsWith("@dimen")) {
                realValue = dimenMap.get(key);
            } else if (value.startsWith("@color")) {
                realValue = colorMap.get(key);
                System.out.println("Color Map is not created yet!");
            } else if (value.startsWith("@android:color")) {
                realValue = sdkColorMap.get(key);
            }
            else {
                realValue = key;
            }
        } else {
            realValue = value;
        }
        return  realValue;
    }
}
