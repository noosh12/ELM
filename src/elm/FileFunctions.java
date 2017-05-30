package elm;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
//import java.util.Objects;

public class FileFunctions
{

    public static void main( String [] args )
    {
    	int totalQuantity=0, count=0, methodListIndex;
    	
        List<OrderItem> orderLine = new ArrayList<>(); //stores the OrderItem objects
        
    	ArrayList<String> GMDNames = new ArrayList<String>(); //names of each menu item
    	ArrayList<Integer> GMDQuantities = new ArrayList<Integer>(); //quantities ordered of each menu item

    	List<String> methodList = new ArrayList<String>(); //names of each shipping method
    	List<List<String>> methods = new ArrayList<List<String>>(); //holds the orders of each shipping method

        
        // printing introductory lines
        System.out.println("GYM MEALS DIRECT");
        System.out.println();
        System.out.print("loading file GMD.csv ...");
        
        
        try
        {       	        	
            BufferedReader input = new BufferedReader(new FileReader("GMD.csv"));//Buffered Reader object instance with FileReader
            
            System.out.println(" file loaded");
            System.out.print("building objects...");

            String fileRead = input.readLine();
            fileRead = input.readLine(); //2nd line is 1st true line

            // loop until all lines are read
            while (fileRead != null)
            {
                // using string.split to load a string array with the values from each line of
                // the file, using a comma as the delimiter and ignoring commas within quotation marks
                String[] tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            	//assigning our split values into variables
                String discountCode = tokenize[12];  //coupon that was used             
                String shippingMethod = tokenize[14]; //shipping method that was used                       
            	int lineItemQuantity = Integer.parseInt(tokenize[16]); //quantity of current item
                String lineItemName = tokenize[17];	//product name of current item
                String lineItemSKU = tokenize[20]; //SKU of current item (i.e. GMD-12)
                String shippingName = tokenize[34]; //Shipping Name provided by customer
                String shippingAddress1 = tokenize[36]; //Shipping Address provided
                String shippingCity = tokenize[39];	//Shipping city provided (suburb)
                String notes=tokenize[44]; //notes provided by customer regarding shipping       

                // creating temporary instance of an Order object
                OrderItem tempObj = new OrderItem(discountCode, shippingMethod, lineItemQuantity, lineItemName, lineItemSKU, shippingName, shippingAddress1, shippingCity, notes);
                
                // add object to object ArrayList
                orderLine.add(tempObj);
                         
                // read next line before looping
                // if end of file reached 
                fileRead = input.readLine();
                
                count+=1;
//                System.out.print("built object ");
//                System.out.println(count);
            }

            
            input.close(); // closing file stream
            System.out.println(" "+count+" objects built");
            System.out.println();
        }
        // handling exceptions
        catch (FileNotFoundException fnfe)
        {
            System.out.println("error: file not found!");
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

        
        /*
         * Here we loop through all of our order objects to obtain useful info out of them
         */
        for (OrderItem each : orderLine)
        {
        	String tempSKU;
        	int SKU;
        	
        	//Simple total meals ordered counter
        	totalQuantity +=each.getLineItemQuantity(); 
        	
        	//Resolving SKU string into an integer variable (i.e. "GMD-12" -> 12)
        	tempSKU=each.getLineItemSKU();
        	SKU = Integer.parseInt(tempSKU.replaceAll("[\\D]", ""));//remove all non-number characters        	
        	 
        	/*
        	 * Totalling the quantity and name of each menu item. Index of the ArrayLists is equivalent to (SKU-1)
        	 * This is done by:
        	 * 1. If item already exists in the ArrayLists, update the quantities and update name if needed
        	 * 2. If item does not exist in the ArrayLists, but SKU is next element, add the 
        	 * 		item to the ArrayLists and update quantity
        	 * 3. If item does not exist in the ArrayLists, fill ArrayList with temporary dummy info until
        	 * 		we reach relevant SKU element, then add item to ArrayLists and update quantity
        	 */
        	if(SKU-1<GMDQuantities.size()){	//on condition that the ArrayList already has that index
        		GMDQuantities.set(SKU-1, GMDQuantities.get(SKU-1)+each.getLineItemQuantity());//add quantity to item quantity      		
        		if(GMDNames.get(SKU-1).equals("N/A")) //if the current item name set is "N/A' replace with real name
        			GMDNames.set(SKU-1, each.getLineItemName());
        	}
        	else if(SKU-1==GMDQuantities.size()){	//on condition that the ArrayList is up to that index
        		GMDQuantities.add(each.getLineItemQuantity());
        		GMDNames.add(each.getLineItemName());
        	}
        	else{		//on condition that the ArrayList does not have and is not up to that index yet
        		for(int i=GMDQuantities.size();i<SKU;i++){	//looping through creating values of 0 and null until we reach desired index
        			GMDQuantities.add(0);
        			GMDNames.add("N/A");
        		}
        		GMDQuantities.set(SKU-1, each.getLineItemQuantity());	//adding quantity to quantities ArrayList
        		GMDNames.set(SKU-1,each.getLineItemName()); //adding names to names ArrayList
        	}
        	
        	
        	/*
        	 * Storing the different shipping methods and the different orders to each shipping method
        	 */
        	String shippingMethod=each.getShippingMethod().toLowerCase(); //get shippingMethod of current item in lower-case
        	if (shippingMethod != null && !shippingMethod.isEmpty()) { //ensuring that we're on the orderLine that contains the shippingMethod
            	methodListIndex=methodList.indexOf(shippingMethod);//stores index of the current shippingMethod into the List.
            	
                if(methodListIndex==-1){ //if shipping method does not exist in the List
                	methods.add(new ArrayList<String>());//create a new ArrayList in our List
                	methodList.add(shippingMethod);//Adding the shipping method to the shipping method List              	
                	methodListIndex=methodList.indexOf(shippingMethod);//Finds index of just added shipping method.
                	//add the order details to the relevant shipping method list in our list of lists
                	methods.get(methodListIndex).add(each.getShippingMethod()+","+each.getShippingName()+","+each.getShippingAddress()+","+each.getNotes());               	
                }
                else{
                	//add the order details to the relevant shipping method list in our list of lists
                	methods.get(methodListIndex).add(each.getShippingMethod()+","+each.getShippingName()+","+each.getShippingAddress()+","+each.getNotes());               	
                } 
        	}
        }
                
        
        System.out.print("Calculating meal totals...");
        PrintMealTotals(GMDQuantities,GMDNames,totalQuantity);//Prints the totals of each meal
        
        System.out.print("Calculating ingredient totals...");
        CalcPrintIngredients(GMDQuantities,GMDNames); //Calculate the ingredients required
        
        System.out.print("Printing sorted delivery methods...");
        PrintShipping(methodList, methods); //Print the shipping details of each order of each method
        
        

    }
    
//    public static void PerformOrderOperations(ArrayList<OrderItem> orderLine){
//    	
//    }
    
    
    /*
     *   Writes the total sold quantities of each menu item  
     *   to file '_meal_totals.csv'
     */
    
    public static void PrintMealTotals(ArrayList<Integer> quantities, ArrayList<String> names, int total){
            
        try
        {
            // creating a BufferedWriter instance with FileWriter
            // the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file 
            BufferedWriter totals = new BufferedWriter(new FileWriter("_meal_totals.csv", false));

            totals.write("TOTAL MEALS:"+","+total); //Writes total number of meals
            totals.newLine();
            totals.newLine();
            totals.newLine();
            totals.write("NAME"+","+"TOTAL"+","+"MEAL");
            totals.newLine();
            
            //Write the quantities of each meal to file 
            for(int i=0; i<quantities.size(); i++){           	
            	totals.write(names.get(i)+","+quantities.get(i)+","+"GMD-"+(i+1));
            	totals.newLine();
            }

            totals.close(); //close the output file
            System.out.println(" Done!");
        }
        // handle exceptions
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        } 
    }
    
    /*
     * This method calculates the quantities of each ingredient and prints them to file.
     * File will be '_ingredients.csv'
     */
    
    public static void CalcPrintIngredients(ArrayList<Integer> quantities, ArrayList<String> names){
                
        Ingredient chicken = new Ingredient("Chicken");// create temporary instance of Ingredient object        
        Ingredient beef = new Ingredient("Beef");        
        Ingredient sPotato = new Ingredient("Sweet Potato");        
        Ingredient rice = new Ingredient("Rice");        
        Ingredient veg = new Ingredient("Veg");
        
//        List<Ingredient> ingredients = new ArrayList<>(); //creating an ArrayList to hold ingredient objects
//        ingredients.add(chicken);// add to ingredients ArrayList
//        ingredients.add(beef);
//        ingredients.add(sPotato);
//        ingredients.add(rice);
//        ingredients.add(veg);
        

        /*
         * Totalling Ingredient Quantities
         * This is accomplished by searching the menu item name for keywords like large, chicken, rice, etc
         * i.e 'Pepperberry Steak - Large/Rice' would add 200g to the beef and rice quantities
         */
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
          

        /*
         * Writing ingredient quantities to file
         */
        try
        {
            // creating a BufferedWriter instance with FileWriter
            // the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file 
            BufferedWriter ingredientsFile = new BufferedWriter(new FileWriter("_ingredients.csv", false));

            
            ingredientsFile.write("INGREDIENT"+","+"kg");
            ingredientsFile.newLine(); ingredientsFile.newLine(); ingredientsFile.newLine();
            ingredientsFile.write(chicken.getName()+","+(float)chicken.getQuantity()/1000);
            ingredientsFile.newLine();
            ingredientsFile.write(beef.getName()+","+(float)beef.getQuantity()/1000);
            ingredientsFile.newLine(); ingredientsFile.newLine(); ingredientsFile.newLine();
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


    
    /*
     * Prints the orders of each shipping method
     */
    public static void PrintShipping(List<String> methodList, List<List<String>> methods){
    	
    	try
        {
    		// creating a BufferedWriter instance with FileWriter
            // the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file 
            BufferedWriter shipping = new BufferedWriter(new FileWriter("_shipping.csv", false));
            
            shipping.write("SHIPPING METHODS: "+methods.size()); //Writes the total number of shipping methods
        	shipping.newLine(); shipping.newLine();
            
        	//Looping through all shipping methods and within that, looping through all orders while printing
            for(int i=0; i<methods.size(); i++){            	
            	for(int j=0; j<methods.get(i).size(); j++){
            		shipping.newLine();
            		shipping.write(methods.get(i).get(j));	
            	}
            	shipping.newLine();
            	shipping.write("TOTAL: "+methods.get(i).size());
            	shipping.newLine(); shipping.newLine();
            }     

            // close the file
            shipping.close();
            System.out.println(" Done!");
        }
        // handle exceptions
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        } 
    }

}