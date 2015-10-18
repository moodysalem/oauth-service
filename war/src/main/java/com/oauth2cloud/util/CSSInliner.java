package com.oauth2cloud.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSSInliner {
    private static final String STYLE = "style";
    private static final String DELIMS = "{}";
    private static final Logger LOG = Logger.getLogger(CSSInliner.class.getName());

    private static final Priority STYLE_PRIORITY = new Priority(1, 0, 0, 0);

    public static String process(String html) {
        long t1 = System.nanoTime();
        // process the html doc
        Document doc = Jsoup.parse(html);

        // get all the style tags
        Elements styleTags = doc.select(STYLE);

        // this map stores all the styles we need to apply to the elements
        HashMap<Element, HashMap<String, ValueWithPriority>> stylesToApply = new HashMap<>();

        for (Element style : styleTags) {
            String rules = style.getAllElements().get(0).data()
                .replaceAll("\n", "") // remove newlines
                .replaceAll("\\/\\*[^*]*\\*+([^/*][^*]*\\*+)*\\/", "") // remove comments
                .trim();
            StringTokenizer st = new StringTokenizer(rules, DELIMS);
            while (st.countTokens() > 1) {
                String selector = st.nextToken();
                // the list of css styles for the selector
                String properties = st.nextToken();
                String[] splitSelectors = selector.split(",");
                for (String sel : splitSelectors) {
                    Elements selectedElements = doc.select(sel);
                    for (Element selElem : selectedElements) {
                        HashMap<String, ValueWithPriority> existingStyles;
                        if (!stylesToApply.containsKey(selElem)) {
                            existingStyles = stylesOf(STYLE_PRIORITY, selElem.attr(STYLE));
                            stylesToApply.put(selElem, existingStyles);
                        } else {
                            existingStyles = stylesToApply.get(selElem);
                        }

                        stylesToApply.put(selElem,
                            mergeStyle(
                                existingStyles,
                                stylesOf(getPriority(sel), properties)
                            )
                        );
                    }
                }
            }
            style.remove();
        }

        // apply the styles
        for (Element e : stylesToApply.keySet()) {
            for (String property : stylesToApply.get(e).keySet()) {
                e.attr(STYLE, property);
            }
        }

        long t2 = System.nanoTime();
        LOG.log(Level.FINE, "Spent " + ((t2 - t1) / 1000L) + " inlining CSS");
        return doc.toString();
    }

    private static class Priority implements Comparable<Priority> {
        private int a;
        private int b;
        private int c;
        private int d;

        public Priority(int a, int b, int c, int d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }

        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }

        public int getD() {
            return d;
        }

        public void setD(int d) {
            this.d = d;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Priority)) {
                return false;
            }
            Priority other = (Priority) obj;
            return other.getA() == getA() && other.getB() == getB() && other.getC() == getC() && other.getD() == getD();
        }

        @Override
        public int compareTo(Priority o) {
            int comp = Integer.compare(getA(), o.getA());
            if (comp != 0) {
                return comp;
            }
            comp = Integer.compare(getB(), o.getB());
            if (comp != 0) {
                return comp;
            }
            comp = Integer.compare(getC(), o.getC());
            if (comp != 0) {
                return comp;
            }
            return Integer.compare(getD(), o.getD());
        }
    }

    private static class ValueWithPriority {
        private String value;
        private Priority priority;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Priority getPriority() {
            return priority;
        }

        public void setPriority(Priority priority) {
            this.priority = priority;
        }
    }

    private static Priority getPriority(String selector) {
        int b = 0, c = 0, d = 0;
        String[] pieces = selector.split(" ");
        for (String pc : pieces) {
            if (pc.startsWith("#")) {
                b++;
                continue;
            }
            if (pc.contains("[") || pc.startsWith(".") || (pc.contains(":") && (!pc.contains("::")))) {
                c++;
                continue;
            }
            d++;
        }
        return new Priority(0, b, c, d);
    }

    private static HashMap<String, ValueWithPriority> stylesOf(Priority priority, String properties) {
        HashMap<String, ValueWithPriority> vp = new HashMap<>();
        String[] props = properties.split(";");
        for (String p : props) {
            String[] pcs = p.split(":");
            String name = pcs[0], value = pcs[1];
            if (pcs.length != 2) {
                continue;
            }
            ValueWithPriority vwp = new ValueWithPriority();
            vwp.setPriority(priority);
            vwp.setValue(value);
            vp.put(name, vwp);
        }
        return vp;
    }


    private static HashMap<String, ValueWithPriority> mergeStyle(HashMap<String, ValueWithPriority> oldProps,
                                                                 HashMap<String, ValueWithPriority> newProps) {
        HashMap<String, ValueWithPriority> finalProps = new HashMap<>();
        Set<String> allProps = new HashSet<>(oldProps.keySet());
        allProps.addAll(newProps.keySet());
        for (String p : allProps) {
            ValueWithPriority oldValue = oldProps.get(p);
            ValueWithPriority newValue = newProps.get(p);
            if (oldValue == null && newValue == null) {
                continue;
            }
            if (oldValue == null) {
                finalProps.put(p, newValue);
                continue;
            }
            if (newValue == null) {
                finalProps.put(p, oldValue);
                continue;
            }
            int compare = oldValue.getPriority().compareTo(newValue.getPriority());
            if (compare < 0) {
                finalProps.put(p, newValue);
            } else {
                finalProps.put(p, oldValue);
            }
        }
        return finalProps;
    }
}