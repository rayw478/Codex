package raymond.infox;

public enum Categories {

    BAKERY("Bakery"),
    DAIRY("Dairy"),
    GROCERY("Grocery"),
    ORGANIC("Natural Value"),
    HOUSEWARES("Housewares"),
    PHARMACY("Pharmacy"),
    HABA("Health and Beauty"),
    SEAFOOD("Seafood"),
    MEAT("Meat"),
    DELI("Deli"),
    APPAREL("Joe Fresh"),
    ELECTRONICS("Electronics");

    private String friendlyString;

    Categories(String friendlyString) {
        this.friendlyString = friendlyString;
    }

    @Override public String toString() {
        return friendlyString;
    }

    public Categories toCategory(String str) {
        switch(str) {
            case "Bakery":
                return BAKERY;
            case "Dairy":
                return DAIRY;
            case "Grocery":
                return GROCERY;
            case "Natural Value":
                return ORGANIC;
            case "Housewares":
                return HOUSEWARES;
            case "Pharmacy":
                return PHARMACY;
            case "Health and Beauty":
                return HABA;
            case "Seafood":
                return SEAFOOD;
            case "Meat":
                return MEAT;
            case "Deli":
                return DELI;
            case "Joe Fresh":
                return APPAREL;
            case "Electronics":
                return ELECTRONICS;
            default:
                return null;
        }
    }
}
