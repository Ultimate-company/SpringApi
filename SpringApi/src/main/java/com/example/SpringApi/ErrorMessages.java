package com.example.SpringApi;

public class ErrorMessages {
    public static final String InvalidColumn = "Invalid column, the column should be one of the following: ";
    public static final String InvalidAddress = "Invalid address, please check all the address fields entered. Address line 1, address line 2, city, state, zip code are all required fields";
    public static final String InvalidPhone = "Invalid phone number, please check the entered phone number. Phone number should be of 10 numbers";
    public static final String Unauthorized = "You are unauthorized to do this action.";

    public static class SupportErrorMessages {
        // Additional Error messages
        public static final String ER001 = "The ticket has been created but there was an error uploading the attachments to the ticket, please try editing the ticket again in sometime.";
    }

    public static class CarrierErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid Carrier id provided.";

        // Additional error messages
        public static final String ER001 = "Given User and carrier are not mapped.";
        public static final String ER002 = "No Carriers found for given user.";
        public static final String ER003 = "There was an error fetching the issue types from jira.";
        public static final String ER004 = "There was an error fetching the carrier based on the wildcards.";
        public static final String ER005 = "Wilcard and api access key is required.";
        public static final String ER006 = "Invalid Credentials";
    }

    public static class LoginErrorMessages {
        // standard error messages
        public static final String InvalidCredentials = "Invalid Credentials";
        public static final String InvalidId = "Invalid User Id";
        public static final String InvalidEmail = "Invalid User Email";
        public static final String AccountConfirmed = "Account has already been confirmed";
        public static final String InvalidToken = "Invalid token";
        public static final String GoogleUserInconsistency = "User present in google users but not present in system users";
        public static final String AddUser = "There was an error creating the user in the database";
        public static final String Login = "There was an error Logging in";

        // Additional error messages
        public static final String ER001 = "There was an error confirming the user email";
        public static final String ER002 = "Failed to Authenticate User";
        public static final String ER003 = "Cannot Reset a password for Oauth User";
        public static final String ER004 = "There was an error resting the user password";
        public static final String ER005 = "Please Confirm Your Account first";
        public static final String ER006 = "Your account has been locked please reset your password to login";
        public static final String ER007 = "Due to multiple failed attempts your account has been locked please reset your password to unlock your account";
        public static final String ER008 = "Email Exists in System, User has signed up using Oauth";
        public static final String ER009 = "Email Exists in System, User is a customer";
        public static final String ER010 = "Email Exists in System";
        public static final String ER011 = "You Currently do not have any permissions to use the System please contact your admin.";
        public static final String ER012 = "Email and password cannot be null or empty.";
        public static final String ER013 = "Login name, password, first name, last name, phone and date of birth are required in order to sign up.";
        public static final String ER014 = "User email is required in order to reset the password.";
        public static final String ER015 = "User email and api key is required to get the access token.";

    }

    public static class UserLogErrorMessages{
        // standard error messages
    }

    public static class UserErrorMessages{
        // standard error messages
        public static final String Unauthorized = "Current user is not authorized to fetch/update details for the given user";
        public static final String InvalidId = "Invalid User Id";
        public static final String InvalidEmail = "Invalid Email";
        public static final String AddUser = "There was an error adding the User";
        public static final String EditUser = "There was an error editing the User";
        public static final String EmailExists = "The given email already exists in the system.";

        // Additional error messages
        public static final String ER001 = "There should be at least one row present in the imported excel sheet";
        public static final String ER002 = "Maximum 100 users can be imported at a time.";
        public static final String ER003 = "No permission set exists for the given user in the db.";
        public static final String ER004 = "Email is required and should be valid.";
        public static final String ER005 = "First name is required.";
        public static final String ER006 = "Last name is required.";
        public static final String ER007 = "User role is required and should be one of the following: .";
        public static final String ER008 = "Date of birth is required and should be valid.";
        public static final String ER009 = "Phone number is required and should be valid.";
    }

    public static class UserGroupErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid Group Id";
        public static final String GroupNameExists = "Group name exists in the system.";

        // Additional error messages
        public static final String ER001 = "One or more group ids is not valid.";
        public static final String ER002 = "User group name is required.";
        public static final String ER003 = "User group description is required";
        public static final String ER004 = "Atleast one user should be selected to include in the user group.";
    }

    public static class AddressErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid Address Id.";
        public static final String NotFound = "Address not found for the give Id.";

        // Additional error messages
        public static final String ER001 = "Address line 1 is required.";
        public static final String ER002 = "City is required.";
        public static final String ER003 = "State is required.";
        public static final String ER004 = "Zip Code is required.";

    }

    public static class TodoErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid todo Id.";
    }

    public static class MessagesErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid message Id.";

        // Additional error messages
        public static final String ER001 =
                "This message cannot be edited as the email is scheduled within 10 minutes from the current time/";
        public static final String ER002 = "There was an error cancelling the scheduled email.";
        public static final String ER003 = "Message title cannot be empty.";
        public static final String ER004 = "Message description cannot be empty.";
        public static final String ER005 = "Message publish date cannot be empty and needs to be greater than or equal to current date.";
        public static final String ER006 = "Message description markdown cannot be empty";
        public static final String ER007 = "Message description html cannot be empty";
        public static final String ER008 = "Atleast one user/usergroup needs to be present in the message.";
    }

    public static class WebTemplatesErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid web template Id.";
        public static final String UrlExists = "Same Url exists in the db, url should be unique.";

        // Additional error messages
        public static final String ER001 = "At least one sort option should be selected and should be one of the following: \"Price(low to high)\", \"Price(high to low)\", \"Rating\", \"Newest\", \"Oldest\"";
        public static final String ER002 = "At least one product id should be selected, and should be valid.";
        public static final String ER003 = "At least one filter option should be selected and should be one of the following: \"Price Range\", \"Category\", \"Brand\", \"Size\", \"Color\", \"Rating\", \"Availability\"";
        public static final String ER004 = "At least one filter option should be selected and should be one of the following: \"Credit Card\", \"Debit Card\", \"Amazon Pay\", \"Net Banking\", \"UPI\", \"EMI\", \"Gift Cards\", \"Cash on Delivery (COD)\"";
        public static final String ER005 = "Header color is required.";
        public static final String ER006 = "At least one Shipping State is required. A complete list of valid states list can be found here: ";
        public static final String ER007 = "At least one City mapping is required for each state. A complete list of valid state -> city mapping can be found here: ";
        public static final String ER008 = "Url should be present and valid. The url should also be a subdomain of ultimatecompany.com";
        public static final String ER009 = "Card header, card subtext, header - font styles are required. Each font style should have the font style, font color and the font size.";

        public static final String ER010 = "No items are present in the cart for the given userid and productid.";
        public static final String ER011 = "No items are present in the liked items for the given userid and productid.";
    }

    public static class PackageErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid Package Id";

        // Additional error messages
        public static final String ER001 = "Length, breadth and height are required and should be greater than 0";
        public static final String ER002 = "Quantity is required and should be greater than 0";
        public static final String ER003 = "Package with the same dimensions exists in the system, please update the quantity of the same package.";
    }

    public static class PickupLocationErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid pickup location Id.";
        public static final String DuplicateName = "Duplicate pickup location name.";

        // Additional error messages
        public static final String ER001 = "Pickup location is required and should be valid.";
    }

    public static class PromoErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid promo Id.";
        public static final String InvalidName = "Invalid promo code.";
        public static final String DuplicateName = "Duplicate promo code name.";

        // Additional error messages
        public static final String ER001 = "Promo code is required.";
        public static final String ER002 = "Promo description is required.";
        public static final String ER003 = "Promo discount value is required and should be greater than 0.";
    }

    public static class ProductCategoryErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid category Id.";
    }

    public static class ProductErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid product Id.";


        // additional error messages
        public static final String ER001 = "Product title is required.";
        public static final String ER002 = "Product Description plain text and html are required.";
        public static final String ER003 = "Product brand is required.";
        public static final String ER004 = "Product country of manufacture is required.";
        public static final String ER005 = "Product main, top, bottom, front, back, right, left and detail images are required and the urls should be valid.";
        public static final String ER006 = "ItemAvailable from date should not be null and should be greater than or equal to todays date.";
        public static final String ER007 = "Product category id should be present";
    }

    public static class LeadsErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid lead id.";

        // additional error messages
        public static final String ER001 = "Lead email is required.";
        public static final String ER002 = "Lead first name is required.";
        public static final String ER003 = "Lead last name is required.";
        public static final String ER004 = "Lead phone number is required.";
        public static final String ER005 = "Assigned Agent user id should be present in the database.";
        public static final String ER006 = "Website should be correctly formatted.";
        public static final String ER007 = "Invalid lead status, the lead status should be one of the following: ";
    }

    public static class SalesOrderErrorMessages {
        // Standard error messages for sales order operations
        public static final String InvalidId = "Invalid sales order id.";

        // additional error messages
        public static final String ER001 = "Payment id is required and should be valid";
        public static final String ER002 = "Purchase order id is required and should be valid";
        public static final String ER003 = "Terms and conditions html and markdown values are required.";
        public static final String ER004 = "At least one sales order product quantity mapping is required.";
        public static final String ER005 = "Product id should be valid and present in the database.";
        public static final String ER006 = "Error creating the sales order pdf.";

        public static final String ER007 = "Packing estimate model should not be null or empty.";
        public static final String ER008 = "A courier id needs to be selected for shipping.";
    }

    public static class PurchaseOrderErrorMessages {
        // Standard error messages for purchase order operations
        public static final String InvalidId = "Invalid purchase order id.";

        // additional error messages
        public static final String ER001 = "Expected shipment date should be greater than or equal to the current date.";
        public static final String ER002 = "Assigned lead id should be present and cannot be 0";
        public static final String ER003 = "Terms and conditions html value is required.";
        public static final String ER004 = "There should be at least one product, quantity mapping.";
        public static final String ER005 = "Product id should be valid and present in the database and quantity for each product should be greater than 0";
        public static final String ER006 = "Purchase order has already been approved and cannot be approved again by the user";
    }

    public static class ProductReviewErrorMessages {
        // Standard error messages for product review order operations
        public static final String InvalidId = "Invalid product review id.";

        // additional error messages
        public static final String ER001 = "Product Review ratings should be present and should be between 0 and 5";
        public static final String ER002 = "Product Review text is required.";
        public static final String ER003 = "Product Review user id is required and should be valid.";
        public static final String ER004 = "Product Review product id is required and should be valid.";
    }

    public static class PaymentInfoErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid payment Info Id.";

        // additional error messages
        public static final String ER001 = "Numbers dont add up this should be true: (subTotal + tax + serviceFee + deliveryFee + packingFee - discount = total)";
    }

}
