# CSV file processor for [Gym Meals Direct](https://www.gymmealsdirect.com.au/) & [Easy Life Meals](https://www.easylifemeals.com.au/)

## Foreword
This program was developed mainly to test and implement things I'm learning at uni. There may be more optimum ways of doing things, but I mainly took this as an opportunity to actually apply skills I was learning to grasp them better.
If I were to approach this program from scratch, I would likely do it differently knowing what I know now.

## Overview
Websites GymMealsDirect and EasyLifeMeals have been developed using the Shopify platform.
Shopfiy [exports order data](https://help.shopify.com/manual/orders/export-orders) as a [Comma Seperated Value file (.csv)](https://help.shopify.com/manual/products/import-export).

This program processes the csv export for these businesses and extracts useful information required for the operation of these businesses.

## What does it do?
Program creates its own csv exports with useful information and metrics like:
* Order Item Quantities
* Meal Type Quantities
* Sauce Type Quantities
* Ingredient Quantities
* Orders info per Vendor
* Notification of orders that contains items no longer sold (yes, this happens)
* Notification of orders that contains items with incorrect SKU (meant to be unique identifier but it's not)
* Forward on relevant shipping notes to delivery drivers.

## Challenges
Coding for fringe use cases that none of us knew would ever happen. They seem really unlikely, but they do break things.

For Gym Meals Direct, customers can order through their preferred gym or fitness chain (vendor). They pick up their order from their chosen vendor. Orders needed to be allocated to each vendor. Names and number of vendors is always changing so program had to dynamically adapt to changing business relationships.

For customers who order through a vendor, they can accidentally place orders for meals that are no longer on the menu. Even worse, the SKUs (unique identifier) are re-used for the meal that has replaced the old meal. Identifying the duplicates and notifying management so they avoid providing the customer with the incorrect meal (the SKU associated with it points to the new meal, not the old as it should).

The products get changed after the weekly cutoff (Thursday midnight). There are situations where customers order just after or before the change is made and wish to be in the current weekly run. As the SKUs are shifted, the product they ordered might still exist, but it may exist under a different SKU. The program had to identify this problem meals, allocate them to the correct total, and notify management of the SKU conflict, like what was done for the old meals.
