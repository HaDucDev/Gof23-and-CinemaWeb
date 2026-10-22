package com.td.CinemaMini.model;

import java.math.BigDecimal;

/** Giá vé trong mẫu suất chiếu; được sao chép để chỉnh giá khi tạo một suất mới bằng Prototype. */
public class PricingConfiguration {
    /** Giá cơ bản mặc định cho một ghế trong mẫu suất chiếu, tính bằng đồng. */
    private BigDecimal basePrice;

    /** Khoản phụ thu mặc định cho một ghế VIP, cộng vào giá cơ bản, tính bằng đồng. */
    private BigDecimal vipSurcharge;

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal v) {
        basePrice = v;
    }

    public BigDecimal getVipSurcharge() {
        return vipSurcharge;
    }

    public void setVipSurcharge(BigDecimal v) {
        vipSurcharge = v;
    }

    public PricingConfiguration copy() {
        PricingConfiguration copy = new PricingConfiguration();
        copy.basePrice = basePrice;
        copy.vipSurcharge = vipSurcharge;
        return copy;
    }
}
