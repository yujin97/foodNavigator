package eugene.hku.foodnavigator.dataClass;

import androidx.annotation.NonNull;

public class PriceLevel {
    private int index;
    private String priceLevel;

    public PriceLevel(int index, String priceLevel) {
        this.index = index;
        this.priceLevel = priceLevel;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(String priceLevel) {
        this.priceLevel = priceLevel;
    }

    @NonNull
    @Override
    public String toString() {
        return this.priceLevel;
    }
}
