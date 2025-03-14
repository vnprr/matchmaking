package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.Subscription;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.repository.SubscriptionRepository;
import com.matchmaking.backend.repository.UserRepository;
import com.matchmaking.backend.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Value("${stripe.api.public-key}")
    private String publicKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/create-checkout")
    public Map<String, String> createCheckout(Authentication auth, @RequestParam String plan) {
        String successUrl = "http://localhost:3000/payment/success";
        String cancelUrl = "http://localhost:3000/payment/cancel";

        try {
            Long amount = switch (plan) {
                case "premium" -> 2999L;
                case "vip" -> 4999L;
                default -> 1999L;
            };

            String checkoutUrl = paymentService.createCheckoutSession(plan, amount, successUrl, cancelUrl);
            return Map.of("checkoutUrl", checkoutUrl);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", publicKey);
    }

    @PostMapping("/webhook")
    public void handleWebhook(HttpServletRequest request, @RequestHeader("Stripe-Signature") String sigHeader) {
        String payload = getRequestBody(request);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            throw new RuntimeException("Webhook error: " + e.getMessage());
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutCompleted(event);
                break;
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;
            case "invoice.payment_succeeded":
                handleInvoicePaid(event);
                break;
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }
    }

    private void handleCheckoutCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
        String userEmail = session.getCustomerDetails().getEmail();
        String subscriptionId = session.getSubscription();

        Optional<User> optionalUser = userRepository.findByUsername(userEmail);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Subscription subscription = subscriptionRepository.findByUserId(user.getId()).orElse(new Subscription());
            subscription.setUser(user);
            subscription.setStripeSubscriptionId(subscriptionId);
            subscription.setPlanName(session.getMetadata().getOrDefault("plan", "standard"));
            subscription.setSubscriptionStart(LocalDateTime.now());
            subscription.setSubscriptionEnd(LocalDateTime.now().plusMonths(1));
            subscription.setActive(true);

            subscriptionRepository.save(subscription);
        }
    }

    private void handleSubscriptionDeleted(Event event) {
        com.stripe.model.Subscription subscription = (com.stripe.model.Subscription) event.getDataObjectDeserializer().getObject().orElseThrow();
        String subscriptionId = subscription.getId();

        subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getStripeSubscriptionId().equals(subscriptionId))
                .findFirst()
                .ifPresent(sub -> {
                    sub.setActive(false);
                    subscriptionRepository.save(sub);
                });
    }

    private void handleInvoicePaid(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElseThrow();
        String subscriptionId = invoice.getSubscription();

        subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getStripeSubscriptionId().equals(subscriptionId))
                .findFirst()
                .ifPresent(sub -> {
                    sub.setSubscriptionEnd(sub.getSubscriptionEnd().plusMonths(1));
                    sub.setActive(true);
                    subscriptionRepository.save(sub);
                });
    }

    private String getRequestBody(HttpServletRequest request) {
        StringBuilder payload = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading webhook payload.");
        }
        return payload.toString();
    }
}