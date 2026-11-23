package com.ptit.oj.controller.judge;

public class FloatComparator extends TokenComparator {
    private double epsilon;

    public FloatComparator(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    protected boolean tokenEquals(String e, String a) {
        if (e.equals(a)) return true;
        try {
            double x = Double.parseDouble(e);
            double y = Double.parseDouble(a);
            double diff = Math.abs(x - y);

            return diff <= epsilon || diff <= epsilon * Math.max(Math.abs(x), Math.abs(y));
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    protected String mismatchLabel() {
        return "Lệch quá sai số";
    }
}
