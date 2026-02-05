package com.lofi.lofiapps.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {

  public static String formatRupiah(BigDecimal amount) {
    if (amount == null) {
      return "Rp 0";
    }

    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(new Locale("id", "ID"));
    DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

    symbols.setGroupingSeparator('.');
    symbols.setMonetaryDecimalSeparator(',');
    formatter.setDecimalFormatSymbols(symbols);

    return "Rp " + formatter.format(amount);
  }
}
