package tagit.java.app.job;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    //valid sgtin96 counter
    public static int sgtin96Count = 0;
    //invalid sgtin96 counter
    public static int wrongCount = 0;
    //invalid sgtin96 counter - wrong hexadecimal format
    public static int invalidHexCount = 0;
    //item counter
    public static long itemCounter = 0;


    //converts hexadecimal string to binary string with leading zeros
    private static String hexToBin(String s) {
        String preBin = new BigInteger(s, 16).toString(2);
        Integer length = preBin.length();
        if (length < 8) {
            for (int i = 0; i < 8 - length; i++) {
                preBin = "0" + preBin;
            }
        }
        return preBin;
    }

    //converts decimal string to binary string
    private static String decToBin(String s, int itemReferenceSize) {
        StringBuilder result = new StringBuilder();
        Long number = Long.parseLong(s);
        //System.out.println(number);
        int i = 0;
        while (number > 0) {
            result.append(number % 2);
            i++;
            number = number / 2;
        }

        if (result.length() <= itemReferenceSize) {
            for (int j = result.length(); j < itemReferenceSize; j++) {
                result.append("0");
            }
        }
        return result.reverse().toString();
    }

    //returns header string from binary string
    private static String getHeader(String s) {
        return s.substring(0, 6);
    }

    //returns filter string from binary string
    private static String getFilter(String s) {
        return s.substring(6, 9);
    }

    //returns partition string from binary string
    private static String getPartition(String s) {
        return s.substring(9, 12);
    }

    //returns company prefix string from binary string
    private static String getCompanyPrefix(String s, int companyPref) {
        int companyPrefEnd = 12 + companyPref;
        String companyPrefix = s.substring(12, companyPrefEnd);
        return companyPrefix;
    }

    //returns item reference string from binary string
    private static String getItemReference(String s, int companyPref, int itemRef) {
        int companyPrefEnd = 12 + companyPref;
        int itemRefEnd = companyPrefEnd + itemRef;
        String itemReference = s.substring(companyPrefEnd, itemRefEnd);
        return itemReference;
    }

    //returns serial number string from binary string
    private static String getSerialNumber(String s, int companyPref, int itemRef) {
        int companyPrefEnd = 12 + companyPref;
        int itemRefEnd = companyPrefEnd + itemRef;
        String serialNumber = s.substring(itemRefEnd);
        return serialNumber;
    }

    //This method determines partitions and compares given item reference and company number with binary strings
    private static void partitioning(String s, String itemReference, String companyNumber) {
        int companyPrefixConstant = 0;
        int itemReferenceConstant = 0;

        switch (getPartition(s)) {
            case "000":
                companyPrefixConstant = 40;
                itemReferenceConstant = 4;
                break;
            case "001":
                companyPrefixConstant = 37;
                itemReferenceConstant = 7;
                break;
            case "010":
                companyPrefixConstant = 34;
                itemReferenceConstant = 10;
                break;
            case "011":
                companyPrefixConstant = 30;
                itemReferenceConstant = 14;
                break;
            case "100":
                companyPrefixConstant = 27;
                itemReferenceConstant = 17;
                break;
            case "101":
                companyPrefixConstant = 24;
                itemReferenceConstant = 20;
                break;
            case "110":
                companyPrefixConstant = 20;
                itemReferenceConstant = 24;
                break;
            default:
                wrongCount++;
                break;
        }
        if (decToBin(itemReference, itemReferenceConstant).equals(getItemReference(s, companyPrefixConstant, itemReferenceConstant))
                && decToBin(companyNumber, companyPrefixConstant).equals(getCompanyPrefix(s, companyPrefixConstant))) {
            itemCounter++;
            System.out.println(getSerialNumber(s, companyPrefixConstant, itemReferenceConstant));
        }
    }

    private static void decode(ArrayList<String> array, String itemReference, String companyNumber) {
        for (int i = 0; i < array.size(); i++) {
            String s = array.get(i);
            if (getHeader(s).equals("110000")) {
                sgtin96Count++;
                partitioning(s, itemReference, companyNumber);

            } else {
                wrongCount++;
            }
        }
    }

    //This method counts all invalid values from the .txt file.
    private static void countWrong(ArrayList<String> array) {
        for (int i = 0; i < array.size(); i++) {
            String s = array.get(i);
            if (getHeader(s).equals("110000")) {
                sgtin96Count++;
            } else {
                wrongCount++;
            }
        }
        System.out.println("Valid SGTIN-96 numbers: " + sgtin96Count);
        System.out.println("Invalid SGTIN-96 numbers: " + wrongCount);
        System.out.println("Invalid hexadecimal numbers: " + invalidHexCount);
        System.out.println("Total invalid numbers: " + (wrongCount + invalidHexCount) + "\n");

    }

    //This method opens .txt file and saves all the values to he array list and returns array list.
    private static ArrayList<String> openTextFile(String file) {
        ArrayList<String> bin = new ArrayList<String>();
        ArrayList<String> hex = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;

            System.out.println("File " + file + " was succesfully open.");

            while ((line = in.readLine()) != null) {
                hex.add(line);
                try {
                    bin.add(hexToBin(line));
                } catch (NumberFormatException e) {
                    invalidHexCount++;
                    System.out.println("Number " + line + " isn't valid hexadecimal number!\n");
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Something went wrong: " + e + "\n");
        }
        return bin;
    }

    //This method opens .csv file and saves all the values to the hashmap.
    private static LinkedHashMap<LinkedHashMap<BigInteger, String>, LinkedHashMap<BigInteger, String>> openCsvFile(String file) {

        LinkedHashMap<BigInteger, String> companyHash = new LinkedHashMap<BigInteger, String>();
        LinkedHashMap<BigInteger, String> itemHash = new LinkedHashMap<BigInteger, String>();
        LinkedHashMap<LinkedHashMap<BigInteger, String>, LinkedHashMap<BigInteger, String>> combination = new LinkedHashMap<LinkedHashMap<BigInteger, String>, LinkedHashMap<BigInteger, String>>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;

            System.out.println("File " + file + " was succesfully open.\n");

            in.readLine();
            while ((line = in.readLine()) != null) {
                String[] novi = line.split(";");
                companyHash.put(new BigInteger(novi[0]), novi[1]);
                itemHash.put(new BigInteger(novi[2]), novi[3]);
                combination.put((LinkedHashMap) companyHash.clone(), (LinkedHashMap) itemHash.clone());
                companyHash.clear();
                itemHash.clear();
            }

        } catch (IOException e) {
            System.out.println("Something went wrong: " + e + "\n");
        }
        return combination;
    }

    public static void main(String[] args) {
        String tags = "tags.txt";
        String data = "data.csv";

        Scanner scanner = new Scanner(System.in);
        int itemSelect = 0;
        ArrayList<String> bin = openTextFile(tags);
        countWrong(bin);
        LinkedHashMap<LinkedHashMap<BigInteger, String>, LinkedHashMap<BigInteger, String>> combination = openCsvFile(data);

        System.out.println("List of items: ");

        int i = 0;
        //Print all available items
        for (Map.Entry<LinkedHashMap<BigInteger, String>, LinkedHashMap<BigInteger, String>> entry : combination.entrySet()) {
            for (Map.Entry<BigInteger, String> entry1 : entry.getValue().entrySet()) {
                i++;
                System.out.println(i + ")" + entry1.getValue());
            }
        }

        System.out.println("\nEnter product number: ");
        itemSelect = scanner.nextInt();

        //Select item from list. If you choose number of item that isn't on the list, you have to enter number again.
        if (itemSelect < 1 || itemSelect > combination.size()) {
            do {
                System.out.print("Enter correct product number: \n");
                itemSelect = scanner.nextInt();
            } while (itemSelect < 1 || itemSelect > combination.size());
        }

        //Gets required item reference from hashmap.
        String item = combination.get(combination.keySet().toArray()[itemSelect - 1]).keySet().toString();
        String itemReference = item.substring(1, item.length() - 1);

        //Gets required company number from hashmap.
        String company = (combination.keySet().toArray()[itemSelect - 1]).toString();
        String companyNumber = company.substring(1, company.indexOf("="));

        System.out.println("\nSerial references:");
        decode(bin, itemReference, companyNumber);
        System.out.println("\nNumber of items: " + itemCounter);
    }
}
