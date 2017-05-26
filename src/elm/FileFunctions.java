package elm;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileFunctions
{

    public static void main( String [] args )
    {
    	int totalQuantity=0, count=0;
    	int[] GMD = new int[54];
    	String[] GMDname = new String[54];
    	
    	
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
                // and make temporary variables for the types of data
                String discountCode = tokenize[12];                
                String shippingMethod = tokenize[14];                           
            	int lineItemQuantity = Integer.parseInt(tokenize[16]);
                String lineItemName = tokenize[17];
                String lineItemSKU = tokenize[20];
                String shippingName = tokenize[34];
                String shippingAddress1 = tokenize[36];
                String shippingCity = tokenize[39];
                String notes=tokenize[44];
       

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
            System.out.println(count+" objects built");
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

        // calculations and operations with our objects
        for (Order each : orderItem)
        {
        	String tempSKU, tempName;
        	int SKU;
        	
        	//Simple total meals ordered counter
        	totalQuantity +=each.getLineItemQuantity(); 
        	
        	//Resolving SKU string into an integer variable
        	tempSKU=each.getLineItemSKU();
        	SKU = Integer.parseInt(tempSKU.replaceAll("[\\D]", ""));
        	
        	//Totaling quantity of each item
        	GMD[SKU-1]=GMD[SKU-1]+1;
        	
        	//Building GMD Name String Array
        	tempName=GMDname[SKU-1]; //Getting existing Name stored
        	
        	if (!(tempName != null && !tempName.isEmpty())) {//if no name already stored
        		GMDname[SKU-1]=each.getLineItemName(); //storing name
    		}
        }
        
        PrintTotals(GMD,GMDname,totalQuantity); //Prints the totals of each meal
        
        CalcIngredients(GMD,GMDname); //Calculate the ingredients required
        


    }
    
    public static void PrintTotals(int[] quantityArray, String[] nameArray, int totalQuantity){
        //Printing Total Meals Ordered
        System.out.println();
        System.out.print("Total Meals = ");
        System.out.println(totalQuantity);
        
        //Printing Individual Meal Totals
        System.out.println();
        System.out.println("Meal Totals");
        for(int i=0; i<quantityArray.length; i++){
        	System.out.println("GMD-"+(i+1)+": "+quantityArray[i]+" - "+nameArray[i]);
        }
    }
    
    
    public static void CalcIngredients(int[] quantityArray, String[] nameArray){
    	int chicken=0, beef=0, potato=0, rice=0,veg=0;
    	
        //Determining Ingredient Quantities
        for(int i=0; i<quantityArray.length; i++){
        	String tempName = nameArray[i].toLowerCase(); //Storing current item name

        	if(tempName.contains("large")){
        		if(tempName.contains("chicken"))
            		chicken+=200*quantityArray[i];        		
        		if(tempName.contains("steak"))
            		beef+=200*quantityArray[i];        		
        		if(tempName.contains("potato"))
            		potato+=200*quantityArray[i];       		
        		if(tempName.contains("rice"))
            		rice+=200*quantityArray[i];        		
        		if(tempName.contains("veg"))
            		veg+=180*quantityArray[i];
        	}
        	if(tempName.contains("small")){
        		if(tempName.contains("chicken"))
            		chicken+=150*quantityArray[i];        		
        		if(tempName.contains("steak"))
            		beef+=150*quantityArray[i];       		
        		if(tempName.contains("potato"))
            		potato+=120*quantityArray[i];       		
        		if(tempName.contains("rice"))
            		rice+=120*quantityArray[i];     		
        		if(tempName.contains("veg"))
            		veg+=100*quantityArray[i];
        	}	
        }
        
        //Printing Ingredient Quantities
        System.out.println();
        System.out.println("Ingredient Totals");
        System.out.println("Beef: "+(float)beef/1000+" kg");
        System.out.println("Chicken: "+(float)chicken/1000+" kg");
        System.out.println("Rice: "+(float)rice/1000+" kg");
        System.out.println("Sweet Potato: "+(float)potato/1000+" kg");
        System.out.println("Veg: "+(float)veg/1000+" kg");
    	
    }


}