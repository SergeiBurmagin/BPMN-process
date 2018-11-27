package ru.starfish24.mascotte.utils;

import java.util.LinkedList;
import java.util.List;

public class StringJoinerEx {

    private String crossPairSeparator = ", ";
    private String crossLabelValueSeparator = " ";
    private List<String> labels = new LinkedList<>();
    private List<String> values = new LinkedList<>();

    public static StringJoinerEx create() {
        return new StringJoinerEx();
    }

    public static StringJoinerEx create(String separator) {
        StringJoinerEx stringJoinerEx = new StringJoinerEx();
        stringJoinerEx.crossPairSeparator = separator;
        return stringJoinerEx;
    }

    private StringJoinerEx() {
    }

    public StringJoinerEx add(String label, String value) {
        labels.add(label);
        values.add(value);
        return this;
    }

    public StringJoinerEx add(String value) {
        return add("", value);
    }

    public StringJoinerEx setCrossPairSeparator(String crossPairSeparator) {
        this.crossPairSeparator = crossPairSeparator;
        return this;
    }

    public StringJoinerEx setCrossLabelValueSeparator(String crossLabelValueSeparator) {
        this.crossLabelValueSeparator = crossLabelValueSeparator;
        return this;
    }

    public String toString() {
        if (values.size() == 0) return "";

        List<String> notNullValues = new LinkedList<>();
        List<String> notNullLabels = new LinkedList<>();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (Utils.isEmpty(value)) continue;

            notNullValues.add(value);
            notNullLabels.add(labels.get(i));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < notNullValues.size(); i++) {
            String value = notNullValues.get(i);

            String label = notNullLabels.get(i);
            if (!Utils.isEmpty(label)) {
                sb.append(label).append(crossLabelValueSeparator);
            }
            sb.append(value);

            if (i + 1 != notNullValues.size()) {
                sb.append(crossPairSeparator);
            }
        }
        return sb.toString();
    }
}
