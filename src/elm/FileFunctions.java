package elm;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

public class FileFunctions
{
	public static void main( String [] args )
	{
		int totalQuantity = 0;
		int count = 0;
		String lastOrder = "";
		List<OrderItem> orderLine = new ArrayList<>();							//stores the OrderItem objects
		HashMap<String, Integer> skuQuantities= new HashMap<String,Integer>();	//meal quantities
		HashMap<String, String> skuNames= new HashMap<String,String>();			//meal names
		HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod = new HashMap<String, ArrayList<OrderItem>> (); // Orders by shipping method
		
		ArrayList<String> skuList = new ArrayList<String>(); //Arraylist that holds skus - for sorting
		ArrayList<String> namesList = new ArrayList<String>(); //Arraylist that holds names - for sorting
		ArrayList<String> namesListGMD = new ArrayList<String>(); //Arraylist that holds names - for sorting
		ArrayList<String> namesListSpecials = new ArrayList<String>(); //Arraylist that holds names - for sorting
		HashMap<String, String> namesSkus= new HashMap<String,String>(); 
		
		HashMap<String, String> GRTRskuNames= new HashMap<String,String>();			//meal names
		HashMap<String, String> GRTRnamesSkus= new HashMap<String,String>(); 
		HashMap<String, Integer> GRTRskuQuantities= new HashMap<String,Integer>(); 
		ArrayList<String> GRTRnamesList = new ArrayList<String>(); //Arraylist that holds names - for sorting
		
		
		System.out.println("EASY LIFE MEALS  |  GYM MEALS DIRECT");
		System.out.println();
		System.out.println("loading file input.csv ...");

		try
		{
			BufferedReader input = new BufferedReader(new FileReader("input.csv"));//Buffered Reader object instance with FileReader
			System.out.print("Reading...");
			String fileRead = input.readLine(); // Headers
			fileRead = input.readLine(); //first real line
			
			String[] tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
			while(tokenize.length<56){
				fileRead = fileRead + input.readLine();
				tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
			}	
			lastOrder = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")[15];
			
			while (fileRead != null)
			{
				// split input line on commas, except those between quotes ("")
				tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				
				//Fixing lines that are incorrectly ended prematurely due to line break in fields like 'notes'
				while(tokenize.length<56){
					fileRead = fileRead + input.readLine();
					tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				}		

				String orderID = tokenize[0];							//OrderID
				String email = tokenize[1];
				String discountCode = tokenize[12];						//coupon that was used
				String shippingMethod = tokenize[14];					//shipping method that was used
				int lineItemQuantity = Integer.parseInt(tokenize[16]);	//quantity of current item
				String lineItemName = tokenize[17].toUpperCase();						//product name of current item
				String lineItemSKU = tokenize[20];						//SKU of current item (i.e. GMD-12)
				String billingName = tokenize[24];						//Billing Name provided by customer
				String shippingName = tokenize[34];						//Shipping Name provided by customer
				String shippingAddress1 = tokenize[36];					//Shipping Address provided
				String shippingCity = tokenize[39];						//Shipping city provided (suburb)
				String shippingPostcode = tokenize[40].replaceAll("[\\D]", "");
				String shippingPhone = tokenize[43].replaceAll("[\\D]", "");
				String notes=tokenize[44];								//notes provided by customer regarding shipping
				String vendor=tokenize[50];
				
				//if customer has not ticked shipping is same as billing, and has left shipping blank
				if(shippingName == null || shippingName.isEmpty()){
					shippingName = billingName;
				}
				
				//indenting protein ball names so they're seperated from the ordinary meals
				if(lineItemName.toLowerCase().contains("protein balls")){
					lineItemName = " " + lineItemName;
				}
				if(lineItemName.toLowerCase().contains("raw bars")){
					lineItemName = "  " + lineItemName;
				}
				if(vendor.toLowerCase().contains("snack")){
					lineItemName = " " + lineItemName;
				}
				if(lineItemName.toLowerCase().contains("pancake")){
					lineItemName = "_" + lineItemName;
				}
				if(lineItemName.toLowerCase().contains("omelette")){
					lineItemName = "__" + lineItemName;
				}
				
				//build orderLine object from chosen extracted values
				orderLine.add(
					new OrderItem(orderID, discountCode, shippingMethod, lineItemQuantity, lineItemName,
						lineItemSKU, billingName, shippingAddress1, shippingCity, shippingPostcode, notes, shippingPhone, email, vendor)
				);

				fileRead = input.readLine();
				count+=1;
			}

			input.close();
			System.out.println(" " + count + " objects built");
			System.out.println();
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("error: file not found!");
//			System.exit(1);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
		
		//GRTR
		System.out.println("loading file input_grtr.csv ...");
		try
		{
			BufferedReader input = new BufferedReader(new FileReader("input_grtr.csv"));//Buffered Reader object instance with FileReader
			System.out.print("Reading...");
			String[] GRTRmealNames = input.readLine().toUpperCase().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // meal names
			String[] GRTRskus = input.readLine().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");; // meal names
			int firstMealIndex = 0;
			
			String fileRead = input.readLine(); //first real line
			
			for(int i = 0; i<GRTRmealNames.length; i++){
				if(GRTRmealNames[i] == null || GRTRmealNames[i].isEmpty()){
					firstMealIndex++;
					continue;
				}
				GRTRnamesSkus.put(GRTRmealNames[i], GRTRskus[i]);
				GRTRskuNames.put(GRTRskus[i], GRTRmealNames[i]);
				GRTRnamesList.add(GRTRmealNames[i]);
				GRTRskuQuantities.put(GRTRskus[i], 0);
			}
			
			while (fileRead != null)
			{
				
				// split input line on commas, except those between quotes ("")
				String[] tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				
				while(tokenize.length<12){
					fileRead = fileRead + input.readLine();
					tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				}				

				String orderID = "GRTR-" + tokenize[0];							//OrderID
				String email = "";
				String discountCode = "";
				String shippingMethod = "GRTR Delivery";					//shipping method that was used
				int lineItemQuantity = 0;	//quantity of current item
				String lineItemName = "";						//product name of current item
				String lineItemSKU = "";						//SKU of current item (i.e. GMD-12)
				String billingName = tokenize[1]+" "+tokenize[2];						//Billing Name provided by customer
				String shippingName = billingName;						//Shipping Name provided by customer
				String shippingAddress1 = tokenize[3];					//Shipping Address provided
				String shippingCity = tokenize[5];						//Shipping city provided (suburb)
				String shippingPostcode = tokenize[4];
				String shippingPhone = "";
				String notes=tokenize[7];								//notes provided by customer regarding shipping
				String vendor="GRTR";
			
				
				for(int i=firstMealIndex; i<tokenize.length; i++){
					if(tokenize[0] == null || tokenize[0].isEmpty())
						continue;
					
					if(!tokenize[i].equals("0") && tokenize[i] != null && !	tokenize[i].isEmpty()) {
						lineItemQuantity = Integer.parseInt(tokenize[i]);
						lineItemName = GRTRmealNames[i].toUpperCase();
						lineItemSKU = GRTRskus[i];

						GRTRskuQuantities.put(lineItemSKU, GRTRskuQuantities.get(lineItemSKU) + lineItemQuantity);
						
						orderLine.add(
								new OrderItem(orderID, discountCode, shippingMethod, lineItemQuantity, lineItemName,
										lineItemSKU, billingName, shippingAddress1, shippingCity, shippingPostcode, notes, shippingPhone, email, vendor)
						);
					}
				}

				fileRead = input.readLine();
			}

			input.close();
			System.out.println(" Done!");
			System.out.println();
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("No GRTR input found");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
		
		int giftCardCount=0, orderCount=0;
		int smalls = 0, larges = 0, snacks = 0;
		String oldOrderID = "NOTAREALID";		
		
		// Here we loop through all of our order objects to fully build our hashmaps
		for (OrderItem order : orderLine)
		{
			String sku = order.getLineItemSKU(); //sku of current orderLine
			String name = order.getLineItemName();
			totalQuantity += order.getLineItemQuantity(); //Total items sold counter

			//Skip to next orderLine object if order is a gift card
			//This is because gift cards don't contain a sku, and as such, give us problems later on if we process it
			//this is because the blank sku isn't really blank, but is "" which cannot be identified easily
			if(order.getLineItemName().toLowerCase().contains("gift card")){
				giftCardCount++;
				continue;
			}
			orderCount++; //Processed orders (gift card orders are NOT processed
			
			// Storing the different shipping methods and the different orders to each shipping method
			if (!(order.getOrderID().equals(oldOrderID))){
				String shippingMethod = order.getShippingMethod().toLowerCase();
				if (!ordersByShippingMethod.containsKey(shippingMethod)){
					ordersByShippingMethod.put(shippingMethod, new ArrayList<OrderItem>());		
				}
				ordersByShippingMethod.get(shippingMethod).add(order);
				oldOrderID=order.getOrderID();
			}
			
			if(order.getVendor().equals("GRTR"))
				continue;
			
			if(order.getLineItemName().toLowerCase().contains("large"))
				larges+=order.getLineItemQuantity();
			if(order.getLineItemName().toLowerCase().contains("small"))
				smalls+=order.getLineItemQuantity();
			if(order.getLineItemName().toLowerCase().contains("red cross"))
				smalls+=order.getLineItemQuantity();
			if(order.getLineItemName().toLowerCase().contains("meals on wheels"))
				smalls+=order.getLineItemQuantity();
			if(order.getVendor().toLowerCase().contains("snack") || order.getVendor().toLowerCase().contains("macro"))
				snacks+=order.getLineItemQuantity();
			
			// Tally order quantities in a HashMap on SKU
			if (skuQuantities.containsKey(sku)){	//if sku already exists in hashMap
				name = order.getLineItemName();
				if (name.equals(skuNames.get(sku))){ //if name matches sku value
					skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());
				}			
			} else { //if item does not exist in hashmap yet
				skuQuantities.put(sku, order.getLineItemQuantity()); //Add sku and initial quantity			
				skuList.add(sku); //add sku
				skuNames.put(sku, order.getLineItemName()); //Add sku and name
				
				if(order.getVendor().contains("Gym Meals Direct"))
					namesListGMD.add(name);
				if(order.getVendor().contains("Easy Life Meals"))
					namesListSpecials.add(name);
			}		
					
		}
		System.out.println("Total Meals Count: "+totalQuantity);
		System.out.println("Large Meals Count: "+larges);
		System.out.println("Small Meals Count: "+smalls);
		System.out.println("Snacks Count: "+snacks);
		System.out.println("Gift Cards Count: "+giftCardCount);
		System.out.println(orderCount+"/"+count+" orders processed"); //should be total orders - gift card
		System.out.println();
		
		
		System.out.print("Sorting Meal Names...");
		for(String currentSku: skuList){
			namesSkus.put(skuNames.get(currentSku), currentSku);
			namesList.add(skuNames.get(currentSku));			
		}
		
		skuList.clear();
		Collections.sort(namesList);
		Collections.sort(namesListGMD);
		Collections.sort(namesListSpecials);
		for(String currentName : namesList){
			skuList.add(namesSkus.get(currentName));
		}
		System.out.println(" Done!");


		System.out.print("Printing all meal totals...");
		PrintMealTotals(skuQuantities,skuNames,totalQuantity, skuList, snacks, false, "_meal_totals_FULL.csv", lastOrder);//Prints the totals of each meal
		
		System.out.print("Printing GRTR meal totals...");
		ArrayList<String> GRTRskuList = new ArrayList<>();
		Collections.sort(GRTRnamesList);
		for(String currentName : GRTRnamesList){
			GRTRskuList.add(GRTRnamesSkus.get(currentName));
		}
		PrintMealTotals(GRTRskuQuantities,GRTRskuNames,totalQuantity, GRTRskuList, snacks, false, "_meal_totals_GRTR.csv", lastOrder);//Prints the totals of each meal
		
		
		System.out.print("Printing gmd meal totals...");
		skuList.clear();
		for(String currentName : namesListGMD){
			skuList.add(namesSkus.get(currentName));
		}
		PrintMealTotals(skuQuantities,skuNames,totalQuantity, skuList, snacks, false, "_meal_totals_GMD.csv", lastOrder);//Prints the totals of each meal

		System.out.print("Calculating gmd & grtr ingredient totals...");
		GRTRskuList.addAll(skuList);
		GRTRskuQuantities.putAll(skuQuantities);
		GRTRskuNames.putAll(skuNames);
		ArrayList<String> mealsToBeCalculated = new ArrayList<>();
		mealsToBeCalculated.addAll(namesListGMD);
		mealsToBeCalculated.addAll(GRTRnamesList);
		CalcPrintIngredients(GRTRskuQuantities,GRTRskuNames, GRTRskuList, mealsToBeCalculated); //Calculate the ingredients required
		
		System.out.print("Printing chef specials meal totals...");
		skuList.clear();
		for(String currentName : namesListSpecials){
			skuList.add(namesSkus.get(currentName));
		}
		PrintMealTotals(skuQuantities,skuNames,totalQuantity, skuList, snacks, true, "_meal_totals_SPECIALS.csv", lastOrder);//Prints the totals of each meal
		
		
		System.out.print("Printing sorted delivery methods...");
		PrintShipping(ordersByShippingMethod); //Print the shipping details of each order of each method
	}

	/*
	 *   Writes the total sold quantities of each menu item
	 *   to file '_meal_totals.csv'
	 */
	public static void PrintMealTotals(HashMap<String, Integer> quantities, HashMap<String, String> skuNames, int total, ArrayList<String> skus, int snacks, boolean skip, String fileName, String date){

		ArrayList<String> preferredMealOrderSkus = new ArrayList<>();
		
		boolean kitchen = (fileName.equals("_meal_totals_GMD.csv") || fileName.equals("_meal_totals_GRTR.csv")) ? true : false;
		
		if(fileName.equals("_meal_totals_GMD.csv")){
			//TODO change direction of sku-names
			//temp fix to be removed once sku-names fullly implemented
			HashMap<String, String> namesSkus = new HashMap<>();
			for(String sku: skuNames.keySet()){
				namesSkus.put(skuNames.get(sku), sku);
			}
			
			try
			{
				System.out.println();
				BufferedReader mealOrder = new BufferedReader(new FileReader("input_meal_order.csv"));
				System.out.print("Reading...");
				String meal = mealOrder.readLine(); 
				
				while (meal != null)
				{
					if(meal.contains(",")){
//						System.out.println(meal);
						meal = meal.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")[0];
					}
					preferredMealOrderSkus.add(namesSkus.get(meal.toUpperCase()));
					meal = mealOrder.readLine();
				}
				mealOrder.close();
			}
			catch (FileNotFoundException fnfe)
			{
				System.out.println("error: file not found!");
			} catch (IOException e) {
				System.out.println("error: ioexception!");
			}
			
			ArrayList<String> activeSkus = new ArrayList<>();
			activeSkus.addAll(skus);
			skus.clear();
			
			for(String sku: preferredMealOrderSkus){
				if(activeSkus.contains(sku))
					skus.add(sku);
			}
			
			for(String sku: activeSkus){
				if(!skus.contains(sku))
					skus.add(sku);
			}
		}
		
		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter totals = new BufferedWriter(new FileWriter(fileName, false));
			String mealName;
			String sauceName;
			
			HashMap<String, Integer> sauceTotals = new HashMap<String,Integer>();	//sauce totals
			ArrayList<String> sauces = new ArrayList<String>();
			
			ArrayList<String> itemNames = new ArrayList<String>();
			ArrayList<String> itemSkus = new ArrayList<String>();
			int subtotal = 0;
			int subtotalLarge = 0;
			int subtotalSmall = 0;
			
//			SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd-MM-yyyy");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			System.out.println(date.split("+")[0]);
			Date dateFormatted = dateFormat.parse(date.split(" ")[0]+" "+date.split(" ")[1]);
			
			totals.write(fileName);
			totals.newLine();
			totals.write(dateFormatted.toString());
			totals.newLine();
			totals.newLine();

			if(!fileName.contains("GMD") && !fileName.contains("GRTR")){
				totals.write("TOTAL MEALS: "+ (total-snacks));
				totals.newLine();
				totals.newLine();
				totals.newLine();
			}
			
			totals.write("NAME"+","+"TOTAL"+","+"MEAL");
			totals.newLine();
			

			// Write the quantities of each meal to file
			for(String sku : skus){
//				String sku = namesSku.get(name);
				
				itemSkus.add(sku);
				String name = skuNames.get(sku);
				itemNames.add(skuNames.get(sku));
				
				totals.write(skuNames.get(sku)+ "," +quantities.get(sku)+ ","+sku);
				if(kitchen)
					totals.write("," + quantities.get(sku)/12 +"C +" + quantities.get(sku)%12);		
				totals.newLine();
				
				subtotal+=quantities.get(sku);
				if(skuNames.get(sku).toUpperCase().contains("LARGE"))
					subtotalLarge += quantities.get(sku);
				if(skuNames.get(sku).toUpperCase().contains("SMALL") || skuNames.get(sku).contains("RED CROSS") || skuNames.get(sku).contains("MEALS ON WHEELS") || skuNames.get(sku).toUpperCase().contains("REGULAR"))
					subtotalSmall += quantities.get(sku);
				
				if(skip)
					continue;								
				
				String[] mealNameSplit = skuNames.get(sku).split(" - ");
				sauceName = mealNameSplit[0];
				
				if (sku.contains("LGE")||sku.contains("SML"))
				{
					if (sauceTotals.containsKey(sauceName)){ //if item already exists in hashMap
						sauceTotals.put(sauceName, sauceTotals.get(sauceName)+quantities.get(sku));					
					}
					else {
						sauceTotals.put(sauceName, quantities.get(sku));
						sauces.add(sauceName);
					}
				}
					
			}
			
			totals.newLine();
			totals.write("SUBTOTAL:"+ ","+(subtotal));

					
			if (!sauceTotals.isEmpty() && !skip){
							
				//Writing the totals for each sauce type
				totals.newLine();	totals.newLine();	totals.newLine();
				totals.write("SAUCE TOTALS"+","+"LITRES"+","+"TOTAL"+","+"% OF MEALS");				
				for (String sauce : sauces){
					totals.newLine();
					totals.write(sauce +","+String.format("%1$,.2f", sauceTotals.get(sauce)*0.12)+" L"+","+sauceTotals.get(sauce)+","+sauceTotals.get(sauce)*100.0/(total-snacks)+"%");
				}
				totals.newLine(); totals.newLine();
				totals.write("Average"+","+","+(total-snacks)/sauces.size()+","+(((total-snacks)/sauces.size())*100.0)/(total-snacks)+"%");
				
			}
			totals.newLine();totals.newLine();
			totals.write(","+"QUANTITY"+","+"%");
			totals.newLine();
			totals.write("Large Meals"+","+subtotalLarge+","+String.format("%1$,.2f", subtotalLarge*100.0/subtotal)+" %");
			totals.newLine();
			totals.write("Small Meals"+","+subtotalSmall+","+String.format("%1$,.2f", subtotalSmall*100.0/subtotal)+" %");
			
			if(fileName.contains("FULL")){
				totals.newLine();
				totals.write("Snacks"+","+snacks+","+String.format("%1$,.2f", snacks*100.0/subtotal)+" %");
			}
				
			totals.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * This method calculates the quantities of each ingredient and prints them to file.
	 * File will be '_ingredients.csv'
	 */
	public static void CalcPrintIngredients(HashMap<String, Integer> quantities, HashMap<String, String> names, ArrayList<String> skus, ArrayList<String> unmatched){
		
		HashMap<String, Ingredient> ingredients = new HashMap<>();
		List<String> errors = new ArrayList<>();
		HashSet<String> ingredientMeals = new HashSet<>();
		
		//TODO change direction of sku-names
		//temp fix to be removed once sku-names fullly implemented
		HashMap<String, String> namesSkus = new HashMap<>();
		for(String sku: names.keySet()){
			namesSkus.put(names.get(sku), sku);
		}
	
		try
		{
			System.out.println();
			BufferedReader ingredientFile = new BufferedReader(new FileReader("input_ingredients.csv"));
			System.out.print("Reading ingredients...");
			String ingredient = ingredientFile.readLine();
			int lineCount = 0;
			
			while (ingredient != null)
			{
				lineCount++;
				double multiplier = 1.0;
				String unit = "kg";
				
				if(ingredient.contains(",")){
					String[] fullLine = ingredient.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
					ingredient = fullLine[0].toUpperCase();
					
					try{
						multiplier = Double.parseDouble(fullLine[1]);
					} catch (Exception e){
						errors.add("Unable to process number from input_ingredient line:"+lineCount+", name: " +ingredient);
						System.out.println("Unable to process number from input_ingredient line:"+lineCount+", name: " +ingredient);
					}
					
					if(fullLine.length >= 3){ //contains unit
						if(!fullLine[2].isEmpty() && fullLine[2] != null){ //valid unit
							unit = fullLine[2];
						}
					}
				} else {				
					ingredient = ingredient.toUpperCase();
				}
				
				ingredients.put(ingredient, new Ingredient(ingredient, multiplier, unit));
				ingredient = ingredientFile.readLine();	
			}
			ingredientFile.close();
			System.out.println(lineCount + " ingredients loaded");
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("error: file not found!");
		} catch (IOException e) {
			System.out.println("error: ioexception!");
		}
		
		try
		{
			System.out.println();
			BufferedReader mealIngredientFile = new BufferedReader(new FileReader("input_meals.csv"));
			System.out.print("Reading...");
			String mealLine = mealIngredientFile.readLine(); 
			int lineCount = 0;
			
			while (mealLine != null)
			{
				lineCount++;
				mealLine=mealLine.toUpperCase();
				try{
					String[] fullLine = mealLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
					ingredientMeals.add(fullLine[0]);
					
					if(namesSkus.containsKey(fullLine[0])){
						String sku = namesSkus.get(fullLine[0]);
						if(!ingredients.containsKey(fullLine[1])){
							String error = "Unable to find ingredient in input_ingredients.csv: "+fullLine[1];
							if(!errors.contains(error)){
								errors.add(error);
								System.out.println(error);
								mealLine = mealIngredientFile.readLine();
								continue;
							}	
						}		
						ingredients.get(fullLine[1]).addQuantity(quantities.get(sku), Double.parseDouble(fullLine[2]));
						
						if(unmatched.contains(fullLine[0])){
							unmatched.remove(fullLine[0]);
						}
					}
				} catch (Exception e){
					String error = "Unable to process meal/ingredient/quantity from input_meals.csv row:"+lineCount+", name: " +mealLine;
					errors.add(error);
					System.out.println(error);
				}
				mealLine = mealIngredientFile.readLine();
			}
			mealIngredientFile.close();
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("error: file not found!");
		} catch (IOException e) {
			System.out.println("error: ioexception!");
		}
		
		if(!unmatched.isEmpty()){
			for(String meal: unmatched){
				errors.add("No meal ingredients found for: "+meal +","+meal);
				System.out.println("No meal ingredients found for: "+meal);
			}
		}

		/*
		 * Totalling Ingredient Quantities
		 */
		ArrayList<String> ingredientNames = new ArrayList<>();
		for(String ingredientName : ingredients.keySet()){
//			System.out.println(ingredientName+"  " +ingredients.get(ingredientName).getQuantity());
			ingredientNames.add(ingredientName);
//			System.out.println(ingredientName);
		}
		Collections.sort(ingredientNames);
		
		// Finding meals in input_meals that didn't exist on this run
		ArrayList<String> ignoredIngredientMeals = new ArrayList<>();
		ignoredIngredientMeals.addAll(ingredientMeals);
		ignoredIngredientMeals.removeAll(names.values());
		Collections.sort(ignoredIngredientMeals);
		
		// Writing ingredient quantities to file
		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter ingredientsFile = new BufferedWriter(new FileWriter("_ingredients.csv", false));

			ingredientsFile.write("INGREDIENT"+","+"QUANTITY"+","+"UNIT");
			ingredientsFile.newLine();


			for(String ingredientName : ingredientNames){
				if(ingredients.get(ingredientName).getQuantity() == 0.0)
					continue;
				
				ingredientsFile.newLine();
				ingredientsFile.write(ingredientName+","+ingredients.get(ingredientName).getFinalQuantity());

			}
			
			ingredientsFile.newLine();
			ingredientsFile.newLine();
			ingredientsFile.write("ERRORS FOUND:"+","+errors.size());
			
			if(!errors.isEmpty()){
				for(String error: errors){
					ingredientsFile.newLine();
					ingredientsFile.write(",,,"+error);
				}
			}
			
			ingredientsFile.newLine();
			ingredientsFile.newLine();
			ingredientsFile.write("MEALS NOT ORDERED:"+","+ignoredIngredientMeals.size());
			
			if(!ignoredIngredientMeals.isEmpty()){
				for(String meal: ignoredIngredientMeals){
					ingredientsFile.newLine();
					ingredientsFile.write(",,,"+meal);
				}
			}

			ingredientsFile.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * Prints the orders of each shipping method
	 */
	public static void PrintShipping(HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod){

		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter shipping = new BufferedWriter(new FileWriter("_shipping.csv", false));
			BufferedWriter notes = new BufferedWriter(new FileWriter("_delivery_notes.csv", false));
			BufferedWriter deliveries = new BufferedWriter(new FileWriter("_deliveries.csv", false));
			BufferedWriter pickupTables = new BufferedWriter(new FileWriter("_pickup_tables.csv", false));
			
			shipping.write("SHIPPING METHODS: " + ordersByShippingMethod.keySet().size());
			shipping.newLine();	shipping.newLine();	shipping.newLine();	shipping.newLine();	shipping.newLine();
			
			notes.write("NAME,ADDRESS,PHONE");
			notes.newLine();
			notes.write("EMAIL,NOTE,METHOD");
			notes.newLine();notes.newLine();notes.newLine();
			
			deliveries.write("Order ID"+","+"Shipping Name"+","+"Shipping Street"+","+"Shipping City"+","+"Postcode"+","+"Mobile"+","+"Notes");
			deliveries.newLine();
			
			ArrayList<Character> chars = new ArrayList<>();
			HashMap<Character, Integer> charCounts = new HashMap<>();
			int pickupTotal = 0;

			//Looping through all shipping methods and within that, looping through all orders while printing
			for(String shippingMethod : ordersByShippingMethod.keySet()){
				shipping.write("     METHOD,     NAME,     ADDRESS,     NOTES,     ORDER ID");
				shipping.newLine();
				for(OrderItem order : ordersByShippingMethod.get(shippingMethod)){
					String shippingString = order.getShippingMethod() + "," + order.getShippingName() + "," + order.getShippingAddress() + ",";
					shippingString = shippingString+order.getNotes()+","+order.getOrderID();				
					shipping.write(shippingString);
					shipping.newLine();
					
					if(shippingMethod.toLowerCase().contains("delivery")){
						if(order.getNotes() != null && !order.getNotes().isEmpty()){
							notes.write(order.getShippingName()+","+order.getShippingAddress()+","+order.getShippingPhone());
							notes.newLine();
							notes.write(order.getEmail()+","+order.getNotes()+","+shippingMethod);
							notes.newLine();notes.newLine();notes.newLine();
						}
						
						deliveries.write(order.getFullShippingString());
						deliveries.newLine();
					}
					
					if(shippingMethod.toLowerCase().contains("pick up")){
						pickupTotal++;
						String pickupName = (order.getShippingName()).toUpperCase();
						int index = 0;
						char surnameInitial = '_';
						if(pickupName != null && !pickupName.isEmpty() && pickupName.contains(" ") &&  pickupName.lastIndexOf(' ') != pickupName.length()-1){
							index = pickupName.lastIndexOf(' ') +1;
//							if(index+1 == pickupName.length() && pickupName.length() > 1)
//								index--;
							surnameInitial = pickupName.charAt(index);
						}
						
						if(!chars.contains(surnameInitial)){
							chars.add(surnameInitial);
							charCounts.put(surnameInitial, 0);
						}
						charCounts.put(surnameInitial, charCounts.get(surnameInitial)+1);
//						System.out.println(pickupName + " -- " + surnameInitial);
					}

				}
				
				shipping.write("     TOTAL: " + ordersByShippingMethod.get(shippingMethod).size());
				shipping.newLine();	shipping.newLine();	shipping.newLine();
			}
			Collections.sort(chars);
			
			String tableString = "";
			int tables = 7;
			int boxesPerTable = pickupTotal / tables;
//			System.out.println(boxesPerTable);
			int currentTableCount = 0;
			
			pickupTables.write("Total Pickups"+","+pickupTotal);
			pickupTables.newLine();
			pickupTables.write("Min per table"+","+boxesPerTable);
			pickupTables.newLine();
			pickupTables.newLine();
			pickupTables.write("CHARACTERS"+","+"TOTAL");
			pickupTables.newLine();
			
			for (char character: chars){ 
//				System.out.println(character);
				currentTableCount += charCounts.get(character);
				tableString += character;
				
				if(currentTableCount >= boxesPerTable){
					pickupTables.write(tableString + "," + currentTableCount);
					pickupTables.newLine();
					currentTableCount = 0;
					tableString = "";
				}
				
			}
			pickupTables.close();

			deliveries.close();
			shipping.close();
			notes.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(0);
		}

	}
}
