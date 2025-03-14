// java
package com.matchmaking.backend.service;

import com.stripe.Stripe;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public PaymentService(@Value("${stripe.api.secret-key}") String secretKey) {
        Stripe.apiKey = secretKey;
    }

    public String createCheckoutSession(String planName, Long amount, String successUrl, String cancelUrl) throws StripeException {
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.valueOf("card"))
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                        .addLineItem(SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("pln")
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(planName)
                                                                .build())
                                                .setRecurring(
                                                        SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                                                .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                                                .build())
                                                .build())
                                .setQuantity(1L)
                                .build())
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}