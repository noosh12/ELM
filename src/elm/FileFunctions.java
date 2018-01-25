package elm;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

public class FileFunctions
{
	public static void main( String [] args )
	{
		int totalQuantity = 0;
		int count = 0;
		List<OrderItem> orderLine = new ArrayList<>();							//stores the OrderItem objects
		HashMap<String, Integer> skuQuantities= new HashMap<String,Integer>();	//meal quantities
		HashMap<String, String> skuNames= new HashMap<String,String>();			//meal names
		HashMap<String, String> reverseNames = new HashMap<String, String>();
		HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod = new HashMap<String, ArrayList<OrderItem>> (); // Orders by shipping method
		
		ArrayList<String> skus = new ArrayList<String>(); //Arraylist that holds skus - for sorting
		ArrayList<String> names = new ArrayList<String>(); //Arraylist that holds names - for sorting
		HashMap<String, String> Names= new HashMap<String,String>(); //Arraylist for sorted Names
		
		ArrayList<String> orderedOld = new ArrayList<String>(); //order IDs of orders that contain old/discontinued items
		ArrayList<Integer> skusOld = new ArrayList<Integer>(); //skus of old/discontinued items
		ArrayList<String> skusOldNames = new ArrayList<String>(); //skus of old/discontinued items
		
		System.out.println("EASY LIFE MEALS  |  GYM MEALS DIRECT");
		System.out.println();
		System.out.println("loading file input.csv ...");

		try
		{
			BufferedReader input = new BufferedReader(new FileReader("input.csv"));//Buffered Reader object instance with FileReader
			System.out.print("Reading...");
			String fileRead = input.readLine(); // Headers
			fileRead = input.readLine(); //first real line
			

			while (fileRead != null)
			{
				// split input line on commas, except those between quotes ("")
				String[] tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				
				
				//Fixing lines that are incorrectly ended prematurely due to line break in fields like 'notes'
				// System.out.print("line size = "+tokenize.length+"   ");
				while(tokenize.length<56){
					fileRead = fileRead + input.readLine();
					tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
					System.out.println("line size = "+tokenize.length);
				}		

				String orderID = tokenize[0];							//OrderID
				String email = tokenize[1];
				String discountCode = tokenize[12];						//coupon that was used
				String shippingMethod = tokenize[14];					//shipping method that was used
				int lineItemQuantity = Integer.parseInt(tokenize[16]);	//quantity of current item
				String lineItemName = tokenize[17];						//product name of current item
				String lineItemSKU = tokenize[20];						//SKU of current item (i.e. GMD-12)
				String billingName = tokenize[24];						//Billing Name provided by customer
				String shippingName = tokenize[34];						//Shipping Name provided by customer
				String shippingAddress1 = tokenize[36];					//Shipping Address provided
				String shippingCity = tokenize[39];						//Shipping city provided (suburb)
				String shippingPhone = tokenize[43];
				String notes=tokenize[44];								//notes provided by customer regarding shipping
				
				//if customer has not ticked shipping is same as billing, and has left shipping blank
				if(shippingName != null && !shippingName.isEmpty()){
					shippingName = billingName;
				}
				
				//indenting protein ball names so they're seperated from the ordinary meals
				if(lineItemName.toLowerCase().contains("protein balls")){
					lineItemName = " " + lineItemName;
				}
			
				
				//build orderLine object from chosen extracted values
				orderLine.add(
					new OrderItem(orderID, discountCode, shippingMethod, lineItemQuantity, lineItemName,
						lineItemSKU, billingName, shippingAddress1, shippingCity, notes, shippingPhone, email)
				);
				
				//System.out.println("Built Order "+count);
				//System.out.print("reading in new line from file... ");
				fileRead = input.readLine();
				count+=1;
				//System.out.println("line read");
			}

			input.close();
			System.out.println(" " + count + " objects built");
			System.out.println();
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("error: file not found!");
			System.exit(1);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
		
		int giftCardCount=0, orderCount=0;
		int smalls = 0, larges = 0, snacks = 0;
		String oldOrderID = "NOTAREALID";
		
		//Preliminary run to build hashmaps skipping first 1/4 of lines
		//This is because the first and last lines could be problematic
		//if customer ordered before/after menu changed
		for (OrderItem order : orderLine)
		{
			if (orderCount>count/4){ 
				//skipping first quarter of lines in case customer ordered before menu changed
				
				//Skip to next orderLine object if order is a gift card
				//This is because gift cards don't contain a sku, and as such, give us problems later on if we process it
				//this is because the blank sku isn't really blank, but is "" which cannot be identified easily
				if(order.getLineItemName().toLowerCase().contains("gift card")){
					giftCardCount++;
					continue;
				}
				
				String sku = order.getLineItemSKU();
				String name = order.getLineItemName();
				if(!reverseNames.containsKey(name)){
					if(!skuNames.containsKey(sku)){
						skuNames.put(sku, name);
						skuQuantities.put(sku, 0);
						skus.add(sku);
						reverseNames.put(name, sku);
					}
				}
			}
			orderCount++;	
		}
		System.out.println("Preliminary run completed on line "+count/4+" to "+orderCount);
		
		orderCount=0;
		
		// Here we loop through all of our order objects to fully build our hashmaps
		for (OrderItem order : orderLine)
		{
			String sku = order.getLineItemSKU(); //sku of current orderLine
			String name = order.getLineItemName();
			int extraSku;
			totalQuantity += order.getLineItemQuantity(); //Total items sold counter
			//System.out.println(order.getOrderID());
			//System.out.println(order.getLineItemSKU());

			//Skip to next orderLine object if order is a gift card
			//This is because gift cards don't contain a sku, and as such, give us problems later on if we process it
			//this is because the blank sku isn't really blank, but is "" which cannot be identified easily
			if(order.getLineItemName().toLowerCase().contains("gift card")){
				giftCardCount++;
				continue;
			}
			orderCount++; //Processed orders (gift card orders are NOT processed
			
			if(order.getLineItemName().toLowerCase().contains("large"))
				larges+=order.getLineItemQuantity();
			if(order.getLineItemName().toLowerCase().contains("small"))
				smalls+=order.getLineItemQuantity();
			if(order.getLineItemName().toLowerCase().contains("protein ball"))
				snacks+=order.getLineItemQuantity();
			
			
			// Tally order quantities in a HashMap on SKU
			if (skuQuantities.containsKey(sku)){	//if sku already exists in hashMap
				name = order.getLineItemName();
				if (name.equals(skuNames.get(sku))){ //if name matches sku value
					skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());
				}
				else if (reverseNames.containsKey(name)){
					//if hashmap contains this name under a dif sku (extremely rare)
					//occurs when someone orders in between weekly cutoff and menu changed (skus+names)
					//this part covers the use case when the intended sku is already in-use
					//(Part 1 of 2)
					sku = reverseNames.get(name); //set sku to the recorded sku of that menu item
					skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());					
				}
				else{ 
					//if name does not match recorded sku value (less rare) and item name not in hashmap already
					//happens when customer manually orders items not on current menu

					extraSku = Integer.parseInt(sku.replaceAll("[\\D]", "")); //extract integer component of sku
					if(extraSku<10)
					{
						sku = "OLD-0"+extraSku; //duplicate sku item changes prefix to ZZZ i.e GMD-12 -> OLD-12
					}
					else{
						sku = "OLD-"+extraSku; //duplicate sku item changes prefix to ZZZ i.e GMD-12 -> OLD-12
					}
					
					if(!skusOld.contains(extraSku)){
						skusOld.add(extraSku);
						skusOldNames.add(name);
					}
					
					
//					if(skuQuantities.containsKey(sku)){
//						skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());
//					}
//					else{
//						skuQuantities.put(sku, order.getLineItemQuantity());
//						skuNames.put(sku, order.getLineItemName());
//						skus.add(sku);
//						
//					}
					//Shouldn't need the above because items that already exist should be caught in the above else if
					skuQuantities.put(sku, order.getLineItemQuantity());
					skuNames.put(sku, order.getLineItemName());
					skus.add(sku);
					reverseNames.put(order.getLineItemName(), sku); //add name and sku
					
					//orderedOld.add(order.getOrderID()); //Recording order ID of current order
				}				
			} else if (reverseNames.containsKey(name)){ //hashmaps contain name but not line sku
				
				sku = reverseNames.get(name); //set sku to the recorded sku of that menu item
				skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());
				
			} else { //if item does not exist in hashmap yet
				
				skuQuantities.put(sku, order.getLineItemQuantity()); //Add sku and initial quantity
				skuNames.put(sku, order.getLineItemName()); //Add sku and name
				skus.add(sku); //add sku
				reverseNames.put(order.getLineItemName(), sku); //add name and sku
			}		

			// Storing the different shipping methods and the different orders to each shipping method
			if (!(order.getOrderID().equals(oldOrderID))){
				String shippingMethod = order.getShippingMethod().toLowerCase();
				if (!ordersByShippingMethod.containsKey(shippingMethod)){
					ordersByShippingMethod.put(shippingMethod, new ArrayList<OrderItem>());		
				}
				ordersByShippingMethod.get(shippingMethod).add(order);
				oldOrderID=order.getOrderID();
			}		
		}
		System.out.println("Large Meals Count: "+larges);
		System.out.println("Small Meals Count: "+smalls);
		System.out.println("Snacks Count: "+snacks);
		System.out.println("Gift Cards Count: "+giftCardCount);
		System.out.println(orderCount+"/"+count+" orders processed"); //should be total orders - gift card
		System.out.println();
		
//		System.out.println();
//		for(String sku : skus){
//			System.out.println(gmdNames.get(sku)+" - "+gmdQuantities.get(sku));
//		}
//		System.out.println();
		
		//If hashmaps contain old meals, looping through them to determine the real old
		//the sku with the lowest quantity is determined to be the correct old item
		//i.e. if GMD-12 quantity = 10, and OLD-12 has quantity of 100, skus are swapped
		if (!skusOld.isEmpty()){
			System.out.print("Orders contains OLD meals! Determining true OLD meals...");
			for (Integer sku : skusOld){
				String gmdSku="", oldSku="";
				
				if(sku<10)
				{
					gmdSku="GMD-0"+sku;
					oldSku="OLD-0"+sku;
				}
				else{
					gmdSku="GMD-"+sku;
					oldSku="OLD-"+sku;
				}
				
				int gmdQuantity = skuQuantities.get(gmdSku);
				int oldQuantity = skuQuantities.get(oldSku);
				String gmdName = skuNames.get(gmdSku);
				String oldName = skuNames.get(oldSku);
//				System.out.println("current");
//				System.out.println(gmdSku+": "+gmdName+" - "+gmdQuantity);
//				System.out.println(oldSku+": "+oldName+" - "+oldQuantity);						
				
				if (gmdQuantity<oldQuantity){ //Old is the real gmd
					skuQuantities.remove(gmdSku);
					skuNames.remove(gmdSku);
					skuQuantities.remove(oldSku);
					skuNames.remove(oldSku);

					skuQuantities.put(gmdSku, oldQuantity);//making old the real gmd
					skuNames.put(gmdSku, oldName);
					
					skuQuantities.put(oldSku, gmdQuantity);//making gmd the real old
					skuNames.put(oldSku, gmdName);
//					System.out.println("new");
//					System.out.println(gmdSku+": "+oldName+" - "+oldQuantity);
//					System.out.println(oldSku+": "+gmdName+" - "+gmdQuantity);				
					
					if(!skusOldNames.contains(gmdName)){
						skusOldNames.add(gmdName);
					}
					skusOldNames.remove(oldName);
					
				}	
			}
			System.out.println(" Done!");
		}
		
		
		System.out.print("Finding Orders containing OLD meals or mismatched SKU meals... ");
		for (OrderItem order : orderLine){
			if(skusOldNames.contains(order.getLineItemName())){
				if(!orderedOld.contains(order.getOrderID())){
					orderedOld.add(order.getOrderID());
				}
			}
			if(!(order.getLineItemName().equals(skuNames.get(order.getLineItemSKU())))){
				if(!(order.getNotes().contains("**DIF SKUs**"))){
					order.setNotes("**DIF SKUs**"+ order.getNotes());
				}
			}
				
		}
		System.out.println(" Done!");
		
		System.out.print("Sorting Meal Names...");
		for(String currentSku: skus){
			Names.put(skuNames.get(currentSku), currentSku);
			names.add(skuNames.get(currentSku));			
		}
		Collections.sort(names);
		skus.clear();
		for(String currentName : names){
			skus.add(Names.get(currentName));
		}
		System.out.println(" Done!");


		System.out.print("Calculating meal totals...");
		PrintMealTotals(skuQuantities,skuNames,totalQuantity, skus, larges, smalls, snacks);//Prints the totals of each meal

		System.out.print("Calculating ingredient totals...");
		CalcPrintIngredients(skuQuantities,skuNames); //Calculate the ingredients required
		
		System.out.print("Printing sorted delivery methods...");
		PrintShipping(ordersByShippingMethod, orderedOld); //Print the shipping details of each order of each method
	}

	/*
	 *   Writes the total sold quantities of each menu item
	 *   to file '_meal_totals.csv'
	 */
	public static void PrintMealTotals(HashMap<String, Integer> quantities, HashMap<String, String> names, int total, ArrayList<String> skus, int larges, int smalls, int snacks){
		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter totals = new BufferedWriter(new FileWriter("_meal_totals.csv", false));
			String mealName;
			String sauceName;
			boolean duplicates = false;			
			
			int[] typeTotals = new int[20];
			HashMap<String, Integer> sauceTotals = new HashMap<String,Integer>();	//sauce totals
			ArrayList<String> sauces = new ArrayList<String>();
			
			ArrayList<String> itemNames = new ArrayList<String>();
			ArrayList<String> itemSkus = new ArrayList<String>();

			totals.write("TOTAL MEALS: "+ (total-snacks));
			totals.newLine();
			totals.newLine();
			totals.newLine();
			totals.write("NAME"+","+"TOTAL"+","+"MEAL");
			totals.newLine();

			// Write the quantities of each meal to file
			for(String sku : skus){
				
				if (itemSkus.contains(sku)||itemNames.contains(names.get(sku)))
					duplicates = true;
				
					
				itemSkus.add(sku);
				itemNames.add(names.get(sku));
				
				totals.write(names.get(sku)+ "," +quantities.get(sku)+ ","+sku);
				totals.newLine();
				
				
				if (sku.contains("GMD")||sku.contains("OLD")){
					//Totalling the totals for each meal type
					mealName = names.get(sku).toLowerCase();
					if (mealName.contains("steak")){
						if((mealName.contains("rice"))&&(mealName.contains("veg"))){
							if(mealName.contains("large"))
								typeTotals[12]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[13]+=quantities.get(sku);
						}
						else if((mealName.contains("potato"))&&(mealName.contains("veg"))){
							if(mealName.contains("large"))
								typeTotals[16]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[17]+=quantities.get(sku);
						}
						else if(mealName.contains("rice")){
							if(mealName.contains("large"))
								typeTotals[0]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[1]+=quantities.get(sku);
						}
						else if(mealName.contains("potato")){
							if(mealName.contains("large"))
								typeTotals[2]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[3]+=quantities.get(sku);
						}
						else if(mealName.contains("veg")){
							if(mealName.contains("large"))
								typeTotals[4]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[5]+=quantities.get(sku);
						}
					}
					if (mealName.contains("chicken")){
						if((mealName.contains("rice"))&&(mealName.contains("veg"))){
							if(mealName.contains("large"))
								typeTotals[14]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[15]+=quantities.get(sku);
						}
						else if((mealName.contains("potato"))&&(mealName.contains("veg"))){
							if(mealName.contains("large"))
								typeTotals[18]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[19]+=quantities.get(sku);
						}
						else if(mealName.contains("rice")){
							if(mealName.contains("large"))
								typeTotals[6]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[7]+=quantities.get(sku);
						}
						else if(mealName.contains("potato")){
							if(mealName.contains("large"))
								typeTotals[8]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[9]+=quantities.get(sku);
						}
						else if(mealName.contains("veg")){
							if(mealName.contains("large"))
								typeTotals[10]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[11]+=quantities.get(sku);
						}
					}
					
					
					
					String[] mealNameSplit = names.get(sku).split(" - ");
					sauceName = mealNameSplit[0];
					
					if (sauceTotals.containsKey(sauceName)){ //if item already exists in hashMap
						sauceTotals.put(sauceName, sauceTotals.get(sauceName)+quantities.get(sku));					
					}
					else {
						sauceTotals.put(sauceName, quantities.get(sku));
						sauces.add(sauceName);
					}
				}
			}
			if(duplicates){
				totals.newLine();
				totals.write("ERROR! Duplicates found. File may be incorrect!");
				totals.newLine();
			}
			
					
			if (!sauceTotals.isEmpty()){
				//Writing the totals for each meal type
				totals.newLine();
				totals.newLine();
				totals.write("TYPE TOTALS"+","+"LARGE"+","+"SMALL");
				totals.newLine();
				totals.write("Beef + Rice"+","+typeTotals[0]+","+typeTotals[1]);
				totals.newLine();
				totals.write("Beef + Sweet Potato"+","+typeTotals[2]+","+typeTotals[3]);
				totals.newLine();
				totals.write("Beef + Vege"+","+typeTotals[4]+","+typeTotals[5]);
				if(typeTotals[12] !=0 && typeTotals[13] !=0){
					totals.newLine();
					totals.write("Beef + Rice&Vege"+","+typeTotals[12]+","+typeTotals[13]);
				}
				if(typeTotals[16] !=0 && typeTotals[17] !=0){
					totals.newLine();
					totals.write("Beef + Potato&Vege"+","+typeTotals[16]+","+typeTotals[17]);
				}
				totals.newLine();
				totals.write("Chicken + Rice"+","+typeTotals[6]+","+typeTotals[7]);
				totals.newLine();
				totals.write("Chicken + Sweet Potato"+","+typeTotals[8]+","+typeTotals[9]);
				totals.newLine();
				totals.write("Chicken + Vege"+","+typeTotals[10]+","+typeTotals[11]);
				if(typeTotals[14] !=0 && typeTotals[15] !=0){
					totals.newLine();
					totals.write("Chicken + Rice&Vege"+","+typeTotals[14]+","+typeTotals[15]);
				}
				if(typeTotals[18] !=0 && typeTotals[19] !=0){
					totals.newLine();
					totals.write("Chicken + Potato&Vege"+","+typeTotals[18]+","+typeTotals[19]);
				}

							
				//Writing the totals for each sauce type
				totals.newLine();
				totals.newLine();
				totals.newLine();
				totals.write("SAUCE TOTALS"+","+"TOTAL"+","+"LITRES");				
				for (String sauce : sauces){
					totals.newLine();
					totals.write(sauce +","+sauceTotals.get(sauce)+","+String.format("%1$,.2f", sauceTotals.get(sauce)*0.12)+" L");
				}
			}
			totals.newLine();totals.newLine();
			totals.write(","+"QUANTITY"+","+"PERCENTAGE");
			totals.newLine();
			totals.write("Large Meals"+","+larges+","+String.format("%1$,.2f", larges*100.0/total)+" %");
			totals.newLine();
			totals.write("Small Meals"+","+smalls+","+String.format("%1$,.2f", smalls*100.0/total)+" %");
			totals.newLine();
			totals.write("Snacks"+","+snacks+","+String.format("%1$,.2f", snacks*100.0/total)+" %");
				
			totals.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * This method calculates the quantities of each ingredient and prints them to file.
	 * File will be '_ingredients.csv'
	 */
	public static void CalcPrintIngredients(HashMap<String, Integer> quantities, HashMap<String, String> names){
		
		List<Ingredient> ingredients = new ArrayList<>();	
		
		//todo fix this
		ingredients.add(new Ingredient("Chicken")); 	//0
		ingredients.add(new Ingredient("Beef"));		//1
		ingredients.add(new Ingredient("Mince"));		//2	
		ingredients.add(new Ingredient("Lamb"));		//3
		ingredients.add(new Ingredient("Barramundi"));	//4
		ingredients.add(new Ingredient("Basa"));		//5
		ingredients.add(new Ingredient("Rice"));		//6
		ingredients.add(new Ingredient("Sweet Potato"));//7
		ingredients.add(new Ingredient("Veg"));			//8
		
		
		/*
		 * Totalling Ingredient Quantities
		 * This is accomplished by searching the menu item name for keywords like large, chicken, rice, etc
		 * i.e 'Pepperberry Steak - Large/Rice' would add 200g to the beef and rice quantities
		 */
		for(String sku : quantities.keySet()){
			String tempName = names.get(sku).toLowerCase();

			if(tempName.contains("large")){
				if(tempName.contains("chicken"))
					ingredients.get(0).addQuantity(quantities.get(sku), 200);				
				if(tempName.contains("steak"))
					ingredients.get(1).addQuantity(quantities.get(sku), 200);	
				if(tempName.contains("meatballs"))
					ingredients.get(2).addQuantity(quantities.get(sku), 200);
				if(tempName.contains("con carne")){
					ingredients.get(2).addQuantity(quantities.get(sku), 200);
					ingredients.get(6).addQuantity(quantities.get(sku), 200);
				}				
				if(tempName.contains("barra"))
					ingredients.get(4).addQuantity(quantities.get(sku), 160);
				if(tempName.contains("basa"))
					ingredients.get(5).addQuantity(quantities.get(sku), 160);
				if(tempName.contains("lamb"))
					ingredients.get(3).addQuantity(quantities.get(sku), 160);
				
				if(tempName.contains("potato"))
					ingredients.get(7).addQuantity(quantities.get(sku), 200);
				if((tempName.contains("veg"))&&(tempName.contains("rice"))){
					ingredients.get(6).addQuantity(quantities.get(sku), 100);
					ingredients.get(8).addQuantity(quantities.get(sku), 100);
				}
				else if((tempName.contains("veg"))&&(tempName.contains("potato"))){
					ingredients.get(7).addQuantity(quantities.get(sku), 100);
					ingredients.get(8).addQuantity(quantities.get(sku), 100);
				}
				else if(tempName.contains("rice"))
					ingredients.get(6).addQuantity(quantities.get(sku), 200);
				else if(tempName.contains("veg"))
					ingredients.get(8).addQuantity(quantities.get(sku), 180);
			}
			if(tempName.contains("small")){
				if(tempName.contains("chicken"))
					ingredients.get(0).addQuantity(quantities.get(sku), 150);				
				if(tempName.contains("steak"))
					ingredients.get(1).addQuantity(quantities.get(sku), 150);	
				if(tempName.contains("meatballs"))
					ingredients.get(2).addQuantity(quantities.get(sku), 150);
				if(tempName.contains("con carne")){
					ingredients.get(2).addQuantity(quantities.get(sku), 150);
					ingredients.get(6).addQuantity(quantities.get(sku), 150);
				}				
				if(tempName.contains("barra"))
					ingredients.get(4).addQuantity(quantities.get(sku), 110);
				if(tempName.contains("basa"))
					ingredients.get(5).addQuantity(quantities.get(sku), 110);
				if(tempName.contains("lamb"))
					ingredients.get(3).addQuantity(quantities.get(sku), 120);
				
				if(tempName.contains("potato"))
					ingredients.get(7).addQuantity(quantities.get(sku), 120);
				if((tempName.contains("veg"))&&(tempName.contains("rice"))){
					ingredients.get(6).addQuantity(quantities.get(sku), 70);
					ingredients.get(8).addQuantity(quantities.get(sku), 70);
				}
				else if((tempName.contains("veg"))&&(tempName.contains("potato"))){
					ingredients.get(7).addQuantity(quantities.get(sku), 70);
					ingredients.get(8).addQuantity(quantities.get(sku), 70);
				}
				else if(tempName.contains("rice"))
					ingredients.get(6).addQuantity(quantities.get(sku), 120);
				else if(tempName.contains("veg"))
					ingredients.get(8).addQuantity(quantities.get(sku), 100);
			}
		}

		// Writing ingredient quantities to file
		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter ingredientsFile = new BufferedWriter(new FileWriter("_ingredients.csv", false));

			ingredientsFile.write("INGREDIENT"+","+"kg");
			ingredientsFile.newLine();

			for (Ingredient ingri : ingredients)
			{
				ingredientsFile.newLine();
				ingredientsFile.write(ingri.getName()+","+(float)ingri.getQuantity()/1000);
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
	public static void PrintShipping(HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod, ArrayList <String> old){

		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter shipping = new BufferedWriter(new FileWriter("_shipping.csv", false));
			BufferedWriter notes = new BufferedWriter(new FileWriter("_delivery_notes.csv", false));
			shipping.write("SHIPPING METHODS: " + ordersByShippingMethod.keySet().size());
			shipping.newLine();
			shipping.newLine();
			shipping.newLine();
			shipping.newLine();
			shipping.newLine();
			notes.write("NAME,ADDRESS,PHONE");
			notes.newLine();
			notes.write("EMAIL,NOTE,METHOD");
			notes.newLine();
			notes.newLine();
			notes.newLine();


			//Looping through all shipping methods and within that, looping through all orders while printing
			for(String shippingMethod : ordersByShippingMethod.keySet()){
				shipping.write("     METHOD,     NAME,     ADDRESS,     NOTES,     ORDER ID");
				shipping.newLine();
				for(OrderItem order : ordersByShippingMethod.get(shippingMethod)){
					String shippingString = order.getShippingMethod() + "," + order.getShippingName() + "," + order.getShippingAddress() + ",";
					if(old.contains(order.getOrderID()))
						shippingString = shippingString + "**CONTAINS OLD** "+order.getNotes()+","+order.getOrderID();
					else
						shippingString = shippingString+order.getNotes()+","+order.getOrderID();
								
					shipping.write(shippingString);
					shipping.newLine();
					
					if(shippingMethod.toLowerCase().contains("delivery")){
						if(order.getNotes() != null && !order.getNotes().isEmpty()){
							notes.write(order.getShippingName()+","+order.getShippingAddress()+","+order.getShippingPhone());
							notes.newLine();
							notes.write(order.getEmail()+","+order.getNotes()+","+shippingMethod);
							notes.newLine();
							notes.newLine();
							notes.newLine();
						}
					}					
				}
				
				shipping.write("     TOTAL: " + ordersByShippingMethod.get(shippingMethod).size());
				shipping.newLine();
				shipping.newLine();
				shipping.newLine();
			}

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
