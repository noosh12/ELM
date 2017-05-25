package elm;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileFunctions
{

    public static void main( String [] args )
    {
    	int countGMD1=0,countGMD2=0, countGMD3=0,countGMD4=0,countGMD5=0,countGMD6=0,countGMD7=0,countGMD8=0,countGMD9=0,countGMD10=0;
    	int countGMD11=0,countGMD12=0, countGMD13=0,countGMD14=0,countGMD15=0,countGMD16=0,countGMD17=0,countGMD18=0,countGMD19=0,countGMD20=0;
    	int countGMD21=0,countGMD22=0, countGMD23=0,countGMD24=0,countGMD25=0,countGMD26=0,countGMD27=0,countGMD28=0,countGMD29=0,countGMD30=0;
    	int countGMD31=0,countGMD32=0, countGMD33=0,countGMD34=0,countGMD35=0,countGMD36=0,countGMD37=0,countGMD38=0,countGMD39=0,countGMD40=0;
    	int countGMD41=0,countGMD42=0, countGMD43=0,countGMD44=0,countGMD45=0,countGMD46=0,countGMD47=0,countGMD48=0,countGMD49=0,countGMD50=0;
    	int countGMD51=0,countGMD52=0, countGMD53=0,countGMD54=0;
    	int totalQuantity=0, count=1;
    	
    	
        // create ArrayList to store the order objects
        List<Order> orderItem = new ArrayList<>();
        System.out.println("GYM MEALS DIRECT");
        System.out.println();
        System.out.println("loading file orders_export_103.csv ...");
        
        try
        {
        	
        	// create a Buffered Reader object instance with a FileReader
            BufferedReader br = new BufferedReader(new FileReader("orders_export-103.csv"));
            System.out.println("file loaded");
            System.out.println("building objects...");

            // read the first line from the text file
            String fileRead = br.readLine();
            //2nd line is 1st true line
            fileRead = br.readLine();

            // loop until all lines are read
            while (fileRead != null)
            {
                // use string.split to load a string array with the values from each line of
                // the file, using a comma as the delimiter
                String[] tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // assume file is made correctly
                // and make temporary variables for the three types of data
                String discountCode = tokenize[12];                
                String shippingMethod = tokenize[14];
                                          
            	int lineItemQuantity = Integer.parseInt(tokenize[16]);
                String lineItemName = tokenize[17];
                String lineItemSKU = tokenize[20];
                String shippingName = tokenize[34];
                String shippingAddress1 = tokenize[36];
                String shippingCity = tokenize[39];
                String notes=tokenize[44];
                //System.out.println(tokenize[17]);
            

                // create temporary instance of Order object
                // and load with relevant data values
                Order tempObj = new Order(discountCode, shippingMethod, lineItemQuantity, lineItemName, lineItemSKU, shippingName, shippingAddress1, shippingCity, notes);

                // add to array list
                orderItem.add(tempObj);

                // read next line before looping
                // if end of file reached 
                fileRead = br.readLine();
                
                count+=1;
//                System.out.print("built object ");
//                System.out.println(count);
            }

            // close file stream
            br.close();
            System.out.println("objects built");
        }

        // handle exceptions
        catch (FileNotFoundException fnfe)
        {
            System.out.println("error: file not found!");
        }

        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

        // calculations and operations
        for (Order each : orderItem)
        {
        	
        	totalQuantity +=each.getLineItemQuantity();
        	
        	
        	
//            System.out.println("====================");
//            System.out.println(each);
//            System.out.println();
//            System.out.printf("Total value = %8.2f %n", each.getTotal());
        }
        
        System.out.println();
        System.out.print("Total Quantity = ");
        System.out.println(totalQuantity);

    }

}