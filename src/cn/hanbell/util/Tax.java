/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hanbell.util;

import java.math.BigDecimal;

/**
 *
 * @author C0160
 */
public class Tax {

    protected BigDecimal extax;
    protected BigDecimal taxes;

    public Tax() {
        extax = BigDecimal.ZERO;
        taxes = BigDecimal.ZERO;
    }

    public Tax(BigDecimal extax, BigDecimal taxes) {
        this.extax = extax;
        this.taxes = taxes;
    }

    /**
     * @return the extax
     */
    public BigDecimal getExtax() {
        return extax;
    }

    /**
     * @param extax the extax to set
     */
    public void setExtax(BigDecimal extax) {
        this.extax = extax;
    }

    /**
     * @return the taxes
     */
    public BigDecimal getTaxes() {
        return taxes;
    }

    /**
     * @param taxes the taxes to set
     */
    public void setTaxes(BigDecimal taxes) {
        this.taxes = taxes;
    }
}
