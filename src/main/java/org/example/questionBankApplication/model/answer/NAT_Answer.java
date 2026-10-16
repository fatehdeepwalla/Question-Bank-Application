package org.example.questionBankApplication.model.answer;

import java.math.BigDecimal;
import java.util.Objects;

public class NAT_Answer implements Answer {

    private double minValue;
    private double maxValue;

    public NAT_Answer(double minValue, double maxValue) {
        validate(minValue, maxValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {return maxValue;}

    public void setRange(double minValue, double maxValue) {
        validate(minValue, maxValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    public void validate(double minValue, double maxValue){
        if (!Double.isFinite(minValue) || !Double.isFinite(maxValue)) {
            throw new IllegalArgumentException("NAT answer values must be finite numbers");
        }
        if (minValue > maxValue ) {throw new IllegalArgumentException("Minimum value cannot exceed maximum value");}
    }
}
