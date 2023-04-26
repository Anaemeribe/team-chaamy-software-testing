class Bicycle {

    // state or field
    private int gear = 5;

    public Bicycle()
    {

    }

    // behavior or method
    public void braking() {
        System.out.println("Working of Braking");
    }

    public int getGear() {
        return gear;
    }

    public int setGear(Integer gear) {
        return this.gear = gear.intValue();
    }
}