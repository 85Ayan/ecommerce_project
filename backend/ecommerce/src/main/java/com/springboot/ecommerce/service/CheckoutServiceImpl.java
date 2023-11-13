package com.springboot.ecommerce.service;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.springboot.ecommerce.dao.CustomerRepository;
import com.springboot.ecommerce.dto.PaymentInfo;
import com.springboot.ecommerce.dto.Purchase;
import com.springboot.ecommerce.dto.PurchaseResponse;
import com.springboot.ecommerce.entity.Customer;
import com.springboot.ecommerce.entity.Order;
import com.springboot.ecommerce.entity.OrderItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;



@Service
public class CheckoutServiceImpl implements CheckoutService {

    private CustomerRepository customerRepository;
     
    public CheckoutServiceImpl(CustomerRepository customerRepository,
                                @Value("${stripe.key.secret}") String secretKey ) {
        this.customerRepository = customerRepository;

        //inialize stripe API with secret key
        Stripe.apiKey=secretKey;
    }

    @Override
    @Transactional
    public PurchaseResponse placeOrder(Purchase purchase) {
        

         //retrive the order info from dto
        Order order = purchase.getOrder();

        //generate tracking number 
        String orderTrackingNumber = generateOrderTrackingNumber();
        order.setOrderTrackingNumber(orderTrackingNumber);

        //populate order with order item
        Set<OrderItem> orderItems = purchase.getOrderItems(); 
        orderItems.forEach(item -> order.add(item));

        //populate order with billingAddress and shippingAddress
        order.setBillingAddress(purchase.getBillingAddress());
        order.setShippingAddress(purchase.getShippingAddress());

        //populate customer with order
        Customer customer = purchase.getCustomer();

        //check if is this an exiting customer
        String theEmail = customer.getEmail();

        Customer customerFromDB = customerRepository.findByEmail(theEmail);

        if(customerFromDB != null) {
            //we found them....let's assign them accordingly

            customer = customerFromDB;
        }

        customer.add(order);

        //save to the database
        customerRepository.save(customer);

        //return a response
        return new PurchaseResponse(orderTrackingNumber);



    }

    private String generateOrderTrackingNumber() {
          //generate  a random UUID number

        return UUID.randomUUID().toString();
    }

    @Override
    public PaymentIntent createPaymentIntent(PaymentInfo paymentInfo) throws StripeException {

        List<String> paymentMethodTypes = new ArrayList<>();
        paymentMethodTypes.add("card");


        Map<String, Object> params = new HashMap<>();
        params.put("amount", paymentInfo.getAmount());
        params.put("currency", paymentInfo.getCurrency());
        params.put("payment_method_types", paymentMethodTypes);
        params.put("description", "LUV2SHOP purchase" );
        params.put("receipt_email", paymentInfo.getReceiptEmail());
        
         return PaymentIntent.create(params);
    }

   
}
