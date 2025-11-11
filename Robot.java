public class Robot {
    double armHeightExpected = 0;
    double armLengthExpected = 0;
    boolean grippingBoxExpected = false;
    int detectedErrors = 0;

    static final int MAX_DETECTED_ERRORS = 2;

    double armHeight = 0;
    double armLength = 0;
    boolean grippingBox = false;
    double physicalFailureRate = 0.2;
    double softwareFailureRate = 0.2;

    void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void printDots(int count, long betweenMs) {
        for (int i = 0; i < count; i++) {
            System.out.print(".");
            System.out.flush();
            sleepMs(betweenMs);
        }
        System.out.println();
    }

    void printProgress(String message, int dots, long betweenMs) {
        System.out.print(message);
        printDots(dots, betweenMs);
    }

    // ANSI color helpers
    String red(String s) {
        return "\u001B[31m" + s + "\u001B[0m";
    }

    String green(String s) {
        return "\u001B[32m" + s + "\u001B[0m";
    }

    // Trigger these when we move something
    boolean checkArmHeight = false;
    boolean checkArmLength = false;
    boolean checkGrippingBox = false;

    // ===== Sensors =====
    double getArmHeight() {
        printProgress("Getting arm height ", 3, 150);
        sleepMs(1000);
        if (Math.random() < softwareFailureRate) {
            System.out.println(red("ERROR: Software failure in getArmHeight"));
            printProgress(red("ERROR: Returning incorrect arm height "), 2, 120);
            sleepMs(1000);
            return armHeight * Math.random();
        }
        System.out.println("-> Arm height: " + String.format("%.3f", armHeight));
        return armHeight;
    }

    double getArmLength() {
        printProgress("Getting arm length ", 3, 120);
        sleepMs(1000);
        if (Math.random() < softwareFailureRate) {
            System.out.println(red("ERROR: Software failure in getArmLength"));
            printProgress(red("ERROR: Returning incorrect arm length "), 2, 100);
            sleepMs(1000);
            return armLength * Math.random();
        }
        System.out.println("-> Arm length: " + String.format("%.3f", armLength));
        return armLength;
    }

    boolean isGrippingBox() {
        printProgress("Checking gripper state ", 3, 140);
        sleepMs(1000);
        if (Math.random() < softwareFailureRate) {
            System.out.println(red("ERROR: Software failure in isGrippingBox"));
            printProgress(red("ERROR: Returning incorrect gripping status "), 2, 110);
            sleepMs(1000);
            return !grippingBox;
        }
        System.out.println("-> Gripping box: " + grippingBox);
        return grippingBox;
    }

    // ===== Actuators =====
    void setArmHeight(double height) {
        checkArmHeight = true;
        armHeightExpected = height;

        // Simulate gradual movement
        double start = armHeight;
        double delta = height - start;
        int steps = 4;
        System.out.print("Setting arm height to " + armHeightExpected + " ");
        for (int i = 1; i <= steps; i++) {
            double interim = start + delta * ((double) i / steps);
            System.out.print(String.format("[%.2f]", interim));
            System.out.flush();
            sleepMs(1000);
        }
        System.out.println();

        if (Math.random() < physicalFailureRate) {
            System.out.println(red("ERROR: Physical failure in setArmHeight"));
            printProgress(red("ERROR: Arm height set incorrectly "), 2, 120);
            sleepMs(1000);
            armHeight = height * Math.random();
        } else {
            armHeight = height;
        }
    }

    void setArmLength(double length) {
        checkArmLength = true;
        armLengthExpected = length;

        // Simulate extension/retraction with dots
        String action = length > armLength ? "Extending" : "Retracting";
        printProgress(action + " arm length to " + armLengthExpected + " ", 4, 140);

        if (Math.random() < physicalFailureRate) {
            System.out.println(red("ERROR: Physical failure in setArmLength"));
            printProgress(red("ERROR: Arm length set incorrectly "), 2, 100);
            sleepMs(1000);
            armLength = length * Math.random();
        } else {
            armLength = length;
        }
    }

    void switchBoxGrip() {
        checkGrippingBox = true;
        grippingBoxExpected = !grippingBoxExpected;

        String verb = grippingBoxExpected ? "Closing" : "Opening";
        printProgress(verb + " gripper ", 3, 160);

        if (Math.random() < physicalFailureRate) {
            System.out.println(red("Physical failure in switchBoxGrip"));
            printProgress(red("Box grip state not changed "), 2, 130);
            return;
        }
        grippingBox = !grippingBox;
        System.out.println("-> Gripper now: " + grippingBox);
    }

    void verifySystem() {
        System.out.println("Verifying system integrity");

        if (detectedErrors > MAX_DETECTED_ERRORS) {
            throw new RuntimeException("Too many errors detected! System shutting down.");
        }

        if (checkArmHeight) {
            double measured = getArmHeight();
            if (Double.isNaN(measured) || measured != armHeightExpected) {
                detectedErrors++;
                System.out.println(
                        red("Error detected in arm height: expected=" + armHeightExpected + " measured=" + measured));
                System.out.println(red("Resetting arm height to expected value"));
                sleepMs(1000);
                setArmHeight(armHeightExpected);
                System.out.println("Re-verifying system after correction");
                verifySystem();
            } else {
                System.out.println(green("Arm height OK"));
            }
        }

        if (checkArmLength) {
            double measured = getArmLength();
            if (Double.isNaN(measured) || measured != armLengthExpected) {
                detectedErrors++;
                System.out.println(
                        red("Error detected in arm length: expected=" + armLengthExpected + " measured=" + measured));
                System.out.println(red("Resetting arm length to expected value"));
                sleepMs(1000);
                setArmLength(armLengthExpected);
                System.out.println("Re-verifying system after correction");
                verifySystem();
            } else {
                System.out.println(green("Arm length OK"));
            }
        }

        if (checkGrippingBox) {
            boolean measured = isGrippingBox();
            if (measured != grippingBoxExpected) {
                detectedErrors++;
                System.out.println(red("Error detected in gripping status: expected=" + grippingBoxExpected
                        + " measured=" + measured));
                System.out.println(red("Resetting gripping status to expected value"));
                sleepMs(1000);
                switchBoxGrip();
                System.out.println("Re-verifying system after correction");
                verifySystem();
            } else {
                System.out.println(green("Gripping status OK"));
            }
        }

        checkArmHeight = false;
        checkArmLength = false;
        checkGrippingBox = false;

        if (detectedErrors > MAX_DETECTED_ERRORS) {
            throw new RuntimeException(red("Too many errors detected! System shutting down."));
        }

        // All checks passed
        System.out.println(green("System integrity verified"));
    }

    void fetchBoxFromHeight(double height) {
        System.out.println("Starting box fetch operation");

        printProgress("Raising arm to " + height + "m ", 5, 140);
        setArmHeight(height);
        sleepMs(1000);
        verifySystem();

        printProgress("Extending arm to reach box ", 5, 120);
        setArmLength(1.0);
        sleepMs(1000);
        verifySystem();

        printProgress("Closing gripper to pick box ", 4, 140);
        switchBoxGrip();
        sleepMs(1000);
        verifySystem();

        printProgress("Retracting arm with box ", 5, 110);
        setArmLength(0.0);
        sleepMs(1000);
        verifySystem();

        printProgress("Lowering arm to home position ", 5, 140);
        setArmHeight(0.0);
        sleepMs(1000);
        verifySystem();

        printProgress("Opening gripper to release box ", 4, 140);
        switchBoxGrip();
        sleepMs(1000);
        verifySystem();
    }

    public static void main(String[] args) {
        Robot robot = new Robot();
        try {
            robot.fetchBoxFromHeight(2.0);
            System.out.println("Box fetched successfully!");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
