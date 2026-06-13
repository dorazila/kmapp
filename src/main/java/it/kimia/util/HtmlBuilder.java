package it.kimia.util;

import it.kimia.model.CartItem;

import java.util.List;

import static it.kimia.util.Formatter.*;

/**
 * Generates the offer HTML preview (equivalent to renderOffertaPreviewHtml in JS).
 */
public final class HtmlBuilder {

    private HtmlBuilder() {}

    public static String buildOffertaHtml(
            String num, String today, String cliente, String cantiere,
            String regione, String scadenza, double total,
            List<CartItem> items, String note, String trasporto,
            String agente, String tel, String email,
            boolean shortMode) {

        String rowsHtml = buildRows(items, shortMode);
        String headerHtml = buildHeader(agente, tel, email);
        String footerHtml = buildFooter(agente, tel, email);
        String introHtml  = buildIntro(today, cliente, num, cantiere);

        String priceTh = shortMode ? "" :
            "<th style=\"padding:7px 8px;text-align:right;font-weight:700;\">Prezzo di listino</th>" +
            "<th style=\"padding:7px 8px;text-align:center;font-weight:700;\">Sconto applicato</th>";

        String page1 = """
            <div class="offerta-doc" style="font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#111;background:#fff;max-width:860px;margin:0 auto;">
            """ + headerHtml + introHtml + """
              <div style="padding:8px 32px 20px;">
                <table style="width:100%;border-collapse:collapse;font-size:11px;">
                  <thead>
                    <tr style="background:#1A3A6B;color:#fff;">
                      <th style="padding:7px 6px;text-align:left;font-weight:700;">Articolo Richiesto</th>
            """ + priceTh + """
                      <th style="padding:7px 8px;text-align:right;font-weight:700;">Prezzo netto</th>
                      <th style="padding:7px 8px;text-align:center;font-weight:700;">Quantit&agrave;</th>
                      <th style="padding:7px 8px;text-align:right;font-weight:700;">Totale</th>
                    </tr>
                  </thead>
                  <tbody>""" + rowsHtml + """
                  </tbody>
                </table>
                <div style="margin-top:12px;font-size:11px;font-weight:400;color:#111;text-align:right;">
                  Importo totale: """ + fmtE(total) + """
                </div>
              </div>
              <div style="padding:0 32px 10px;">
                <table style="width:100%;border-collapse:collapse;"><tr>
                  <td style="vertical-align:bottom;font-size:11.5px;">
                    <div><strong>L&rsquo;impresa per accettazione</strong></div>
                    <div style="color:#555;">timbro e firma</div>
                    <div style="margin-top:36px;border-bottom:1px solid #aaa;width:200px;"></div>
                  </td>
                  <td style="text-align:right;vertical-align:bottom;font-size:11.5px;line-height:1.9;">
                    <div>Kimia S.p.A.</div>
                    <div>Il Funzionario d&rsquo;Area</div>
                    <div style="font-weight:600;">""" + escHtml(agente) + """
                    </div>
                    <div style="margin-top:36px;border-bottom:1px solid #aaa;width:180px;margin-left:auto;"></div>
                  </td>
                </tr></table>
              </div>
            """ + footerHtml + "</div>";

        String page2 = """
            <div class="offerta-doc" style="font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#111;background:#fff;max-width:860px;margin:16px auto 0;page-break-before:always;">
            """ + headerHtml + introHtml + """
              <div style="padding:8px 32px 16px;">
                <div style="font-size:12px;font-weight:700;text-transform:uppercase;color:#111;margin-bottom:10px;text-align:center;border-bottom:1px solid #ccc;padding-bottom:6px;">Condizioni di Fornitura</div>
                <div style="font-size:11.5px;line-height:1.8;">
                  <div><strong>Pagamento</strong>: da concordare</div>
                  <div><strong>Fornitura</strong>: Kimia S.p.A.</div>
                  <div><strong>Regione cantiere</strong>: """ + escHtml(regione) + """
                  </div>
                  <div><strong>Validit&agrave; offerta</strong>: """ + escHtml(scadenza) + """
                   giorni</div>
                </div>
                <div style="font-size:11.5px;margin-top:10px;">
                  <strong>Trasporto</strong>: <em>I prezzi sopra indicati non comprendono l&rsquo;incidenza del trasporto</em>,
                  che sar&agrave; contabilizzato come segue:
                </div>
                <div style="font-family:'Courier New',monospace;font-size:10px;background:#f8f8f8;border:1px solid #e0e0e0;border-radius:3px;padding:10px 12px;white-space:pre-wrap;word-break:break-word;margin:8px 0 0;">"""
                + escHtml(trasporto) + """
                </div>
              </div>
            """ +
            (note != null && !note.isBlank() ?
                "<div style=\"padding:0 32px 16px;font-size:11.5px;color:#111;\"><div style=\"margin-top:8px;white-space:pre-wrap;\"><strong>Note:</strong> " + escHtml(note) + "</div></div>" : "") +
            """
              <div style="padding:0 32px 16px;font-size:11.5px;">
                <div><strong>Prezzi da intendersi IVA e trasporto esclusi</strong></div>
                <div style="margin-top:8px;color:#1A3A6B;text-decoration:underline;">Le schede tecniche e le Dop sono scaricabili dal sito www.kimia.it</div>
                <div style="margin-top:12px;">Nel rimanere a disposizione per qualsiasi altra informazione, porgiamo cordiali saluti.</div>
              </div>
            """ + footerHtml + "</div>";

        return wrapPage(page1 + page2);
    }

    private static String buildRows(List<CartItem> items, boolean shortMode) {
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < items.size(); idx++) {
            CartItem r = items.get(idx);
            String bg = idx % 2 == 0 ? "#fff" : "#EBF4FA";
            double prezzoFinale = r.getEffectivePrice();
            Double rowTotal = r.getRowTotal();
            String qtyStr = (r.getQty() != null && r.getQty() > 0)
                    ? fmtQ(r.getQty()) + " " + nvl(r.getUnit_measure()) : "n.d.";
            String scontoStr = (r.getSconto() != null && r.getSconto() > 0)
                    ? String.format("%.0f%%", r.getSconto() * 100) : "";

            String priceCells = shortMode ? "" :
                "<td style=\"padding:7px 8px;text-align:right;vertical-align:top;\">" + fmtN(r.getListino_price()) + "</td>" +
                "<td style=\"padding:7px 8px;text-align:center;vertical-align:top;\">" + escHtml(scontoStr.isEmpty() ? "—" : scontoStr) + "</td>";

            String descr = escHtml(nvl(r.getProduct_name()));
            if (r.getProduct_code() != null && !r.getProduct_code().isBlank()) {
                descr += " [" + escHtml(r.getProduct_code()) + "]";
            }
            String productDescription = nvl(r.getDescription()).trim();
            if (!productDescription.isEmpty()) {
                descr = "<div>" + descr + "</div>" +
                    "<div style=\"margin-top:3px;color:#555;line-height:1.35;\">" + escHtml(productDescription) + "</div>";
            }

            sb.append("<tr style=\"border-bottom:1px solid #e0e0e0;background:").append(bg).append(";\">");
            sb.append("<td style=\"padding:7px 8px;vertical-align:top;\">").append(descr).append("</td>");
            sb.append(priceCells);
            sb.append("<td style=\"padding:7px 8px;text-align:right;vertical-align:top;font-weight:700;\">")
              .append(fmtN(prezzoFinale)).append("</td>");
            sb.append("<td style=\"padding:7px 8px;text-align:center;vertical-align:top;\">")
              .append(escHtml(qtyStr)).append("</td>");
            sb.append("<td style=\"padding:7px 8px;text-align:right;vertical-align:top;font-weight:700;\">")
              .append(rowTotal != null ? fmtE(rowTotal) : "&mdash;").append("</td>");
            sb.append("</tr>");
        }
        return sb.toString();
    }

    private static String buildHeader(String agente, String tel, String email) {
        return """
            <div style="padding:20px 32px 16px;border-bottom:1px solid #ddd;display:flex;align-items:flex-start;justify-content:space-between;">
              <div style="flex:0 0 auto;">
                <span style="font-family:'Arial Black','Segoe UI',Arial,sans-serif;font-size:25.6px;line-height:1;font-weight:800;letter-spacing:.01em;color:#1A3A6B;">KIMIA</span>
              </div>
              <div style="flex:1;text-align:right;font-size:11px;color:#444;line-height:1.7;">
                <div style="font-size:16px;font-weight:800;color:#111;margin-bottom:4px;">Kimia S.p.A.</div>
                <div>Via del Rame, 73</div>
                <div>06134 Perugia (Italia)</div>
                <div>Tel. """ + escHtml(nvl(tel)) + """
                </div>
                <div>E-mail: """ + escHtml(nvl(email)) + """
                </div>
                <div>Website: <span style="color:#1A3A6B;">www.kimia.it</span></div>
                <div style="margin-top:6px;font-weight:600;">""" + escHtml(nvl(agente)) + """
                </div>
                <div>General Sales Manager &ndash; Italia - Export</div>
              </div>
            </div>""";
    }

    private static String buildFooter(String agente, String tel, String email) {
        return """
            <div style="border-top:3px solid #1A3A6B;padding:10px 32px;display:flex;justify-content:space-between;font-size:10px;color:#333;background:#fff;">
              <div><strong>Kimia S.p.A.</strong><br>Via del Rame, 73<br>06134 Perugia (Italia)</div>
              <div><strong>Website:</strong><br><span style="color:#1D9E75;">www.kimia.it</span></div>
              <div><strong>""" + escHtml(nvl(agente)) + """
              </strong><br>General Sales Manager &ndash; Italia - Export</div>
              <div><strong>Tel.</strong> """ + escHtml(nvl(tel)) + """
              <br><strong>E-mail:</strong> """ + escHtml(nvl(email)) + """
              </div>
            </div>""";
    }

    private static String buildIntro(String today, String cliente, String num, String cantiere) {
        String oggetto = "Offerta N. " + escHtml(nvl(num).isEmpty() ? "—" : num);
        if (cantiere != null && !cantiere.isBlank()) oggetto += " - " + escHtml(cantiere);
        return """
            <div style="padding:20px 32px 0;">
              <table style="width:100%;border-collapse:collapse;"><tr>
                <td style="vertical-align:top;"></td>
                <td style="text-align:right;vertical-align:top;font-size:11.5px;line-height:1.7;">
                  <div><strong>Spett.le """ + escHtml(nvl(cliente)) + """
                  </strong></div>
                </td>
              </tr></table>
            </div>
            <div style="padding:10px 32px 16px;font-size:11.5px;">
              <div>""" + escHtml(nvl(today)) + """
              </div>
            </div>
            <div style="padding:0 32px 12px;">
              <div style="font-size:11.5px;"><strong>OGGETTO:</strong> """ + oggetto + """
              </div>
            </div>""";
    }

    private static String wrapPage(String body) {
        return """
            <!DOCTYPE html>
            <html lang="it">
            <head>
            <meta charset="UTF-8">
            <style>
              body { margin:0; background:#f5f5f5; }
              .offerta-doc { box-shadow: 0 2px 8px rgba(0,0,0,0.12); margin-bottom: 16px; }
              @media print {
                body { background:#fff; }
                .offerta-doc { box-shadow:none; }
                .offerta-doc + .offerta-doc { page-break-before:always; }
              }
            </style>
            </head>
            <body>
            """ + body + """
            </body>
            </html>""";
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
