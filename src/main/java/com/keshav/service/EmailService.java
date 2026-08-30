package com.keshav.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.contact.admin-email:keshavkhandelwal240@gmail.com}")
    private String adminEmail;

    @Value("${app.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.resend.from-email:Shoply Support <onboarding@resend.dev>}")
    private String fromEmail;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public EmailService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Sends customer inquiry notification to admin email asynchronously.
     */
    public void sendContactInquiryNotification(String ticketId, String name, String customerEmail, String subject, String message) {
        CompletableFuture.runAsync(() -> {
            try {
                if (resendApiKey == null || resendApiKey.isBlank()) {
                    log.info("[EmailService] No RESEND_API_KEY configured. Contact message logged: [To: {}, Ticket: {}, From: {} ({})]",
                            adminEmail, ticketId, name, customerEmail);
                    return;
                }

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

                String emailSubject = String.format("[Shoply Inquiry #%s] %s - From %s", ticketId, subject, name);

                String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8">
                        <style>
                            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f9fafb; margin: 0; padding: 20px; }
                            .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #e5e7eb; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                            .header { background: linear-gradient(135deg, #f43f5e 0%%, #e11d48 100%%); color: #ffffff; padding: 24px; text-align: left; }
                            .header h1 { margin: 0; font-size: 20px; font-weight: 800; letter-spacing: -0.5px; }
                            .header p { margin: 4px 0 0 0; font-size: 13px; opacity: 0.9; }
                            .body { padding: 24px; color: #1f2937; }
                            .info-row { display: flex; justify-content: space-between; border-bottom: 1px solid #f3f4f6; padding: 10px 0; font-size: 13px; }
                            .label { font-weight: 700; color: #6b7280; text-transform: uppercase; font-size: 11px; }
                            .value { font-weight: 600; color: #111827; }
                            .message-box { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 12px; padding: 16px; margin: 20px 0; font-size: 14px; line-height: 1.6; color: #374151; white-space: pre-wrap; }
                            .footer { background: #f3f4f6; padding: 16px 24px; font-size: 12px; color: #6b7280; text-align: center; }
                            .reply-btn { display: inline-block; background: #f43f5e; color: #ffffff !important; padding: 12px 24px; border-radius: 10px; text-decoration: none; font-weight: 700; font-size: 13px; margin-top: 10px; }
                        </style>
                    </head>
                    <body>
                        <div class="card">
                            <div class="header">
                                <h1>New Customer Support Inquiry</h1>
                                <p>Ticket Reference: <strong>%s</strong> • %s</p>
                            </div>
                            <div class="body">
                                <div class="info-row">
                                    <span class="label">Customer Name</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Customer Email</span>
                                    <span class="value"><a href="mailto:%s" style="color: #f43f5e;">%s</a></span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Topic / Subject</span>
                                    <span class="value">%s</span>
                                </div>

                                <div style="margin-top: 20px;">
                                    <span class="label">Customer's Message:</span>
                                    <div class="message-box">%s</div>
                                </div>

                                <div style="text-align: center; margin-top: 24px;">
                                    <a href="mailto:%s?subject=Re:%%20[Shoply%%20Ticket%%20%s]%%20%s" class="reply-btn">
                                        Reply Directly to %s
                                    </a>
                                </div>
                            </div>
                            <div class="footer">
                                This is an automated notification from your Shoply E-Commerce Storefront.
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(ticketId, timestamp, name, customerEmail, customerEmail, subject, message, customerEmail, ticketId, subject, name);

                Map<String, Object> payload = new HashMap<>();
                payload.put("from", fromEmail);
                payload.put("to", List.of(adminEmail));
                payload.put("reply_to", customerEmail);
                payload.put("subject", emailSubject);
                payload.put("html", htmlContent);

                String requestBody = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.resend.com/emails"))
                        .header("Authorization", "Bearer " + resendApiKey.trim())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(15))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log.info("[EmailService] Contact inquiry #{} delivered to {} successfully!", ticketId, adminEmail);
                } else {
                    log.warn("[EmailService] Resend API responded with status {}: {}", response.statusCode(), response.body());
                }

            } catch (Exception e) {
                log.error("[EmailService] Failed to send contact inquiry notification for ticket #{}: {}", ticketId, e.getMessage());
            }
        });
    }

    /**
     * Sends a wholesale quote notification to the supplier's business email asynchronously.
     */
    public void sendWholesaleQuoteNotification(
            String referenceId,
            String supplierBusinessName,
            String supplierEmail,
            String companyName,
            String contactName,
            String contactEmail,
            String contactPhone,
            int quantity,
            String productName,
            String notes) {

        CompletableFuture.runAsync(() -> {
            try {
                if (resendApiKey == null || resendApiKey.isBlank()) {
                    log.info("[EmailService] No RESEND_API_KEY configured. Wholesale quote logged: [Ref: {}, From: {} <{}>, To supplier: {}]",
                            referenceId, companyName, contactEmail, supplierBusinessName);
                    return;
                }

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
                String productLine = (productName != null && !productName.isBlank())
                        ? "<div class=\"info-row\"><span class=\"label\">Product Interest</span><span class=\"value\">" + productName + "</span></div>"
                        : "";
                String phoneLine = (contactPhone != null && !contactPhone.isBlank())
                        ? "<div class=\"info-row\"><span class=\"label\">Phone</span><span class=\"value\">" + contactPhone + "</span></div>"
                        : "";
                String notesSection = (notes != null && !notes.isBlank())
                        ? "<div style=\"margin-top:20px;\"><span class=\"label\">Buyer Notes:</span><div class=\"message-box\">" + notes + "</div></div>"
                        : "";

                String emailSubject = String.format("[Shoply B2B #%s] New Wholesale Quote Request from %s", referenceId, companyName);

                String htmlContent = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8">
                        <style>
                            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f9fafb; margin: 0; padding: 20px; }
                            .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #e5e7eb; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                            .header { background: linear-gradient(135deg, #7c3aed 0%%, #4f46e5 100%%); color: #ffffff; padding: 24px; text-align: left; }
                            .header h1 { margin: 0; font-size: 20px; font-weight: 800; letter-spacing: -0.5px; }
                            .header p { margin: 4px 0 0 0; font-size: 13px; opacity: 0.9; }
                            .body { padding: 24px; color: #1f2937; }
                            .info-row { display: flex; justify-content: space-between; border-bottom: 1px solid #f3f4f6; padding: 10px 0; font-size: 13px; }
                            .label { font-weight: 700; color: #6b7280; text-transform: uppercase; font-size: 11px; }
                            .value { font-weight: 600; color: #111827; }
                            .qty-badge { display: inline-block; background: #ede9fe; color: #5b21b6; padding: 4px 14px; border-radius: 999px; font-weight: 800; font-size: 15px; }
                            .message-box { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 12px; padding: 16px; margin: 12px 0; font-size: 14px; line-height: 1.6; color: #374151; white-space: pre-wrap; }
                            .footer { background: #f3f4f6; padding: 16px 24px; font-size: 12px; color: #6b7280; text-align: center; }
                            .reply-btn { display: inline-block; background: #7c3aed; color: #ffffff !important; padding: 12px 24px; border-radius: 10px; text-decoration: none; font-weight: 700; font-size: 13px; margin-top: 10px; }
                        </style>
                    </head>
                    <body>
                        <div class="card">
                            <div class="header">
                                <h1>New Wholesale Quote Request</h1>
                                <p>Reference: <strong>%s</strong> &bull; %s</p>
                            </div>
                            <div class="body">
                                <div class="info-row"><span class="label">Supplier</span><span class="value">%s</span></div>
                                <div class="info-row"><span class="label">Company</span><span class="value">%s</span></div>
                                <div class="info-row"><span class="label">Contact Name</span><span class="value">%s</span></div>
                                <div class="info-row"><span class="label">Contact Email</span><span class="value"><a href="mailto:%s" style="color:#7c3aed;">%s</a></span></div>
                                %s
                                %s
                                <div class="info-row"><span class="label">Requested Quantity</span><span class="value"><span class="qty-badge">%d units</span></span></div>
                                %s
                                <div style="text-align:center;margin-top:24px;">
                                    <a href="mailto:%s" class="reply-btn">Reply to %s</a>
                                </div>
                            </div>
                            <div class="footer">Automated notification from Shoply B2B Wholesale Desk.</div>
                        </div>
                    </body>
                    </html>
                    """,
                    referenceId, timestamp,
                    supplierBusinessName, companyName, contactName,
                    contactEmail, contactEmail,
                    phoneLine, productLine, quantity, notesSection,
                    contactEmail, contactName
                );

                Map<String, Object> payload = new HashMap<>();
                payload.put("from", fromEmail);
                payload.put("to", List.of(supplierEmail, adminEmail));
                payload.put("reply_to", contactEmail);
                payload.put("subject", emailSubject);
                payload.put("html", htmlContent);

                String requestBody = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.resend.com/emails"))
                        .header("Authorization", "Bearer " + resendApiKey.trim())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(15))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log.info("[EmailService] Wholesale quote #{} notification sent to {} and {}.", referenceId, supplierEmail, adminEmail);
                } else {
                    log.warn("[EmailService] Resend API responded with status {}: {}", response.statusCode(), response.body());
                }

            } catch (Exception e) {
                log.error("[EmailService] Failed to send wholesale quote notification #{}: {}", referenceId, e.getMessage());
            }
        });
    }
}
