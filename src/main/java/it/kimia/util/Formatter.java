package it.kimia.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class Formatter {

    private static final Locale IT = Locale.ITALIAN;

    private static final NumberFormat EUR_FMT;
    private static final NumberFormat NUM_FMT;
    private static final NumberFormat QTY_FMT;

    static {
        EUR_FMT = NumberFormat.getNumberInstance(IT);
        EUR_FMT.setMinimumFractionDigits(2);
        EUR_FMT.setMaximumFractionDigits(2);

        NUM_FMT = NumberFormat.getNumberInstance(IT);
        NUM_FMT.setMinimumFractionDigits(2);
        NUM_FMT.setMaximumFractionDigits(6);

        QTY_FMT = NumberFormat.getNumberInstance(IT);
        QTY_FMT.setMinimumFractionDigits(0);
        QTY_FMT.setMaximumFractionDigits(4);
    }

    private Formatter() {}

    /** Formats as "€ 1.234,56" */
    public static String fmtE(double n) {
        if (Double.isNaN(n) || Double.isInfinite(n)) return "—";
        return "€ " + EUR_FMT.format(n);
    }

    /** Formats as "1.234,567890" */
    public static String fmtN(double n) {
        if (Double.isNaN(n) || Double.isInfinite(n)) return "";
        return NUM_FMT.format(n);
    }

    /** Formats quantity, up to 4 decimal places */
    public static String fmtQ(double n) {
        if (Double.isNaN(n) || Double.isInfinite(n)) return "";
        return QTY_FMT.format(n);
    }

    /** Parse Italian decimal string (comma as decimal separator) */
    public static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try {
            return Double.parseDouble(s.replace(",", ".").replace(" ", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
