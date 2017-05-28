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
    	ArrayList<String> GMDNames = new ArrayList<String>();
    	ArrayList<Integer> GMDQuantities = new ArrayList<Integer>();
    	String details;
    	ArrayList<String> deliveries = new ArrayList<String>();
    	
    	

    	
    	
        // create ArrayList to store the order objects
        List<Order> orderItem = new ArrayList<>();
        // printing intro lines
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
        	String tempSKU;
        	int SKU;
        	
        	//Simple total meals ordered counter
        	totalQuantity +=each.getLineItemQuantity(); 
        	
        	//Resolving SKU string into an integer variable
        	tempSKU=each.getLineItemSKU();
        	SKU = Integer.parseInt(tempSKU.replaceAll("[\\D]", ""));
        	
        	        	
        	if(SKU-1<GMDQuantities.size()){		//on condition that arraylist already has that index
        		GMDQuantities.set(SKU-1, GMDQuantities.get(SKU-1)+each.getLineItemQuantity());
        		
        		if(GMDNames.get(SKU-1).equals("N/A"))
        			GMDNames.set(SKU-1, each.getLineItemName());
        	}
        	else if(SKU-1==GMDQuantities.size()){		//on condition that arraylist is up to that index
        		GMDQuantities.add(each.getLineItemQuantity());
        		GMDNames.add(each.getLineItemName());
        	}
        	else{		//on condition that arraylist does not have that and is not up to that index yet
        		for(int i=GMDQuantities.size();i<SKU;i++){	//looping through creating values of 0 or null until we reach desired index
        			GMDQuantities.add(0);
        			GMDNames.add("N/A");
        		}
        		GMDQuantities.set(SKU-1, each.getLineItemQuantity());	//adding quantity to index
        		GMDNames.set(SKU-1,each.getLineItemName()); //adding name to index
        	}
        }
        System.out.print("Calculating meal totals...");
        PrintMealTotals(GMDQuantities,GMDNames,totalQuantity);//Prints the totals of each meal
        
        System.out.print("Calculating ingredient totals...");
        CalcPrintIngredients(GMDQuantities,GMDNames); //Calculate the ingredients required
        
        System.out.print("Printing sorted delivery methods...");
        PrintDeliveries(deliveries);
        
        
//        List<Ingredient> ingredients = new ArrayList<>();
//        ingredients=CalcPrintIngredients(GMDQuantities,GMDNames);
//        
//        for(int i=0; i<ingredients.size(); i++){
//        	System.out.println(ingredients.get(i));
//        }
        

    }
    
    public static void PrintMealTotals(ArrayList<Integer> quantities, ArrayList<String> names, int total){
             
        try
        {
            // create Bufferedwriter instance with a FileWriter
            // the flag set to 'true' tells it to append a file if file exists
            BufferedWriter totals = new BufferedWriter(new FileWriter("meal_totals.csv", false));

            totals.write("TOTAL MEALS:"+","+total);

            totals.newLine();
            totals.newLine();
            totals.write("NAME"+","+"TOTAL"+","+"MEAL");
            totals.newLine();
            
            for(int i=0; i<quantities.size(); i++){           	
            	totals.write(names.get(i)+","+quantities.get(i)+","+"GMD-"+(i+1));
            	totals.newLine();
            }

            totals.close();
            System.out.println(" Done!");
        }
        // handle exceptions
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        } 
    }
    
    
    public static void CalcPrintIngredients(ArrayList<Integer> quantities, ArrayList<String> names){
                
        Ingredient chicken = new Ingredient("Chicken");// create temporary instance of Ingredient object        
        Ingredient beef = new Ingredient("Beef");        
        Ingredient sPotato = new Ingredient("Sweet Potato");        
        Ingredient rice = new Ingredient("Rice");        
        Ingredient veg = new Ingredient("Veg");
        
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(chicken);// add to ingredients Arraylist
        ingredients.add(beef);
        ingredients.add(sPotato);
        ingredients.add(rice);
        ingredients.add(veg);
        

        //Determining Ingredient Quantities
        for(int i=0; i<quantities.size(); i++){
        	String tempName = names.get(i).toLowerCase(); //Storing current item name

        	if(tempName.contains("large")){
        		if(tempName.contains("chicken"))
            		chicken.addQuantity(quantities.get(i), 200);       		
        		if(tempName.contains("steak"))
            		beef.addQuantity(quantities.get(i), 200);            		
        		if(tempName.contains("potato"))
            		sPotato.addQuantity(quantities.get(i), 200);            		
        		if(tempName.contains("rice"))
            		rice.addQuantity(quantities.get(i), 200);            		
        		if(tempName.contains("veg"))
            		veg.addQuantity(quantities.get(i), 180);     
        	}
        	if(tempName.contains("small")){
        		if(tempName.contains("chicken"))
            		chicken.addQuantity(quantities.get(i), 150);           		
        		if(tempName.contains("steak"))
            		beef.addQuantity(quantities.get(i), 150);          		
        		if(tempName.contains("potato"))
            		sPotato.addQuantity(quantities.get(i), 120);          		
        		if(tempName.contains("rice"))
            		rice.addQuantity(quantities.get(i), 120);           		
        		if(tempName.contains("veg"))
            		veg.addQuantity(quantities.get(i), 100);     
        	}
        }
          

        //Writing ingredient quantities to file
        try
        {
            // create Bufferedwriter instance with a FileWriter
            // the flag set to 'true' tells it to append a file if file exists
            BufferedWriter ingredientsFile = new BufferedWriter(new FileWriter("ingredients.csv", false));

            // write a `newline` to the file
            ingredientsFile.newLine();
            
            ingredientsFile.write("Ingredient Totals"+","+"kg");
            ingredientsFile.newLine();
            ingredientsFile.write(chicken.getName()+","+(float)chicken.getQuantity()/1000);
            ingredientsFile.newLine();
            ingredientsFile.write(beef.getName()+","+(float)beef.getQuantity()/1000);
            ingredientsFile.newLine();
            ingredientsFile.write(rice.getName()+","+(float)rice.getQuantity()/1000);
            ingredientsFile.newLine();
            ingredientsFile.write(sPotato.getName()+","+(float)sPotato.getQuantity()/1000);
            ingredientsFile.newLine();
            ingredientsFile.write(veg.getName()+","+(float)veg.getQuantity()/1000);

            // close the file
            ingredientsFile.close();
            System.out.println(" Done!");
        }
        // handle exceptions
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        
        //return ingredients;
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