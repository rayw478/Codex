package raymond.infox;

public enum Department {

    BAKERY("Bakery"),
    DAIRY("Dairy"),
    PRODUCE("Produce"),
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

    Department(String friendlyString) {
        this.friendlyString = friendlyString;
    }

    @Override public String toString() {
        return friendlyString;
    }

}
