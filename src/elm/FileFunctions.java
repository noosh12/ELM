package elm;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
//import java.util.Objects;

public class FileFunctions
{

    public static void main( String [] args )
    {
    	int totalQuantity=0, count=0;
    	int[] GMD = new int[54];
    	String[] GMDname = new String[54];
    	String details;
    	ArrayList<String> deliveries = new ArrayList<String>();
    	
    	
        // create ArrayList to store the order objects
        List<Order> orderItem = new ArrayList<>();
        System.out.println("GYM MEALS DIRECT");
        System.out.println();
        System.out.print("loading file orders_export_103.csv ...");
        
        try
        {       	
        	// create a Buffered Reader object instance with a FileReader
            BufferedReader input = new BufferedReader(new FileReader("GMD.csv"));
            System.out.println(" file loaded");
            System.out.print("building objects...");

            // read the first line from the text file
            String fileRead = input.readLine();
            //2nd line is 1st true line
            fileRead = input.readLine();

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
                //System.out.println(lineItemName+": "+lineItemQuantity);
                
                // add to object Arraylist
                orderItem.add(tempObj);
                
                //building deliveries arraylists
                
                if(shippingMethod.equals("Monday Delivery")){
                	details = shippingMethod+","+shippingName+","+shippingAddress1+" "+shippingCity+","+notes;
                	deliveries.add(details);
                }

                // read next line before looping
                // if end of file reached 
                fileRead = input.readLine();
                
                count+=1;
//                System.out.print("built object ");
//                System.out.println(count);
            }

            // close file stream
            input.close();
            System.out.println(" "+count+" objects built");
            System.out.println();
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
        	GMD[SKU-1]=GMD[SKU-1]+each.getLineItemQuantity();
        	
        	//Building GMD Name String Array
        	tempName=GMDname[SKU-1]; //Getting existing Name stored
        	
        	if (!(tempName != null && !tempName.isEmpty())) {//if no name already stored
        		GMDname[SKU-1]=each.getLineItemName(); //storing name
    		}
        }
        System.out.print("Calculating meal totals...");
        PrintTotals(GMD,GMDname,totalQuantity); //Prints the totals of each meal
        System.out.print("Calculating ingredient totals...");
        CalcIngredients(GMD,GMDname); //Calculate the ingredients required
        System.out.print("Printing sorted delivery methods...");
        PrintDeliveries(deliveries);

    }
    
    public static void PrintTotals(int[] quantityArray, String[] nameArray, int totalQuantity){
             
        try
        {
            // create Bufferedwriter instance with a FileWriter
            // the flag set to 'true' tells it to append a file if file exists
            BufferedWriter totals = new BufferedWriter(new FileWriter("totals.csv", false));

            // write the text string to the file
            totals.write("TOTAL MEALS:"+","+totalQuantity);

            // write a `newline` to the file
            totals.newLine();
            totals.newLine();
            totals.write("NAME"+","+"TOTAL"+","+"MEAL");
            totals.newLine();
            
            for(int i=0; i<quantityArray.length; i++){           	
            	//totals.write("GMD-"+(i+1)+","+quantityArray[i]+","+nameArray[i]);
            	totals.write(nameArray[i]+","+quantityArray[i]+","+"GMD-"+(i+1));
            	totals.newLine();
            }

            // close the file
            totals.close();
            System.out.println(" Done!");
        }
        // handle exceptions
        catch (IOException ioe)
        {
            ioe.printStackTrace();
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
//        	System.out.println();
//        	System.out.println(tempName+": "+quantityArray[i]+"    Chicken: "+chicken);
        }
        
        //Writing ingredient quantities to file
        try
        {
            // create Bufferedwriter instance with a FileWriter
            // the flag set to 'true' tells it to append a file if file exists
            BufferedWriter ingredients = new BufferedWriter(new FileWriter("ingredients.csv", false));

            // write a `newline` to the file
            ingredients.newLine();
            
            ingredients.write("Ingredient Totals"+","+"kg");
            ingredients.newLine();
            ingredients.write("Beef"+","+(float)beef/1000);
            ingredients.newLine();
            ingredients.write("Chicken"+","+(float)chicken/1000);
            ingredients.newLine();
            ingredients.write("Rice"+","+(float)rice/1000);
            ingredients.newLine();
            ingredients.write("Sweet Potato"+","+(float)potato/1000);
            ingredients.newLine();
            ingredients.write("Veg"+","+(float)veg/1000);

            // close the file
            ingredients.close();
            System.out.println(" Done!");
        }
        // handle exceptions
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        
    	
    }


    public static void PrintDeliveries(ArrayList<String> deliv){
    	
    	try
        {
            // create Bufferedwriter instance with a FileWriter
            // the flag set to 'true' tells it to append a file if file exists
            BufferedWriter deliveries = new BufferedWriter(new FileWriter("deliveries.csv", false));

            // write the text string to the file
            deliveries.write("TOTAL DELIVERIES:"+","+deliv.size());
            deliveries.newLine();
            deliveries.write("Type"+","+"Name"+","+"Address"+","+"Notes");
            
            for(int i=0; i<deliv.size(); i++){
            	deliveries.newLine();
            	deliveries.write(deliv.get(i));
            }

            // close the file
            deliveries.close();
            System.out.println(" Done!");
        }
        // handle exceptions
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        } 
    }
}