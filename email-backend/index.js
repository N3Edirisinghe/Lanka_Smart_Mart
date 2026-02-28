require('dotenv').config();
const express = require('express');
const cors = require('cors');
const nodemailer = require('nodemailer');
const admin = require('firebase-admin');
const Stripe = require('stripe');

const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

// Initialize Stripe dynamically within the endpoint to prevent Vercel boot crashes
// if the environment variable evaluates to undefined too early.

// Initialize Firebase Admin (You will get this JSON from Firebase Console -> Project Settings -> Service Accounts)
// In Vercel, store this JSON string in an Environment Variable called FIREBASE_SERVICE_ACCOUNT
if (!admin.apps.length) {
    try {
        if (process.env.FIREBASE_SERVICE_ACCOUNT) {
            const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
            admin.initializeApp({
                credential: admin.credential.cert(serviceAccount)
            });
            console.log("Firebase Admin Initialized successfully.");
        } else {
            console.warn("FIREBASE_SERVICE_ACCOUNT is missing. Password reset functions will fail, but order emails will still attempt to send.");
        }
    } catch (error) {
        console.error("Firebase Admin Initialization Error", error);
    }
}

// Nodemailer setup (Store credentials in Vercel Environment Variables: SMTP_EMAIL and SMTP_PASSWORD)
// Note: For Gmail, use an App Password, not your real password.
const transporter = nodemailer.createTransport({
    service: 'gmail', // Or 'SendGrid', etc.
    auth: {
        user: process.env.SMTP_EMAIL,
        pass: process.env.SMTP_PASSWORD
    }
});

app.post('/api/send-order-email', async (req, res) => {
    const { email, orderId, totalPrice, items, paymentMethod } = req.body;

    if (!email || !orderId || !items) {
        return res.status(400).send({ success: false, error: 'Missing required order details' });
    }

    try {
        const isCOD = paymentMethod === 'COD';

        // Build items HTML list
        let itemsHtml = '';
        items.forEach(item => {
            itemsHtml += `
                <tr style="border-bottom: 1px solid #eee;">
                    <td style="padding: 10px 0; color: #333;">${item.productName} (x${item.quantity})</td>
                    <td style="padding: 10px 0; text-align: right; color: #333;">Rs. ${item.productPrice.toFixed(2)}</td>
                </tr>
            `;
        });

        const userSubject = isCOD
            ? `Order Confirmed (Cash on Delivery) - #${orderId.substring(0, 8).toUpperCase()}`
            : `Order Confirmed & Paid - #${orderId.substring(0, 8).toUpperCase()}`;

        const userMessage = isCOD
            ? `Thank you for shopping with Lanka Smart Mart. Your order has been successfully placed as <strong>Cash on Delivery</strong>. Please have the cash ready when your order arrives.`
            : `Thank you for shopping with Lanka Smart Mart. Your order has been successfully placed and your payment has been processed.`;

        const mailOptions = {
            from: `"Lanka Smart Mart" <${process.env.SMTP_EMAIL}>`,
            to: email,
            subject: userSubject,
            html: `
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                    <div style="text-align: center; margin-bottom: 20px;">
                        <img src="https://raw.githubusercontent.com/N3Edirisinghe/Lanka_Smart_Mart/main/email-backend/logo.png" alt="Lanka Smart Mart Logo" style="max-width: 150px; height: auto;">
                    </div>
                    <h2 style="color: #2E7D32; text-align: center;">Order Confirmed!</h2>
                    <p style="color: #333; font-size: 16px;">Hello,</p>
                    <p style="color: #333; font-size: 16px;">${userMessage}</p>
                    
                    <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #333; margin-top: 0;">Order Summary</h3>
                        <p style="color: #666; font-size: 14px; margin-bottom: 15px;">Order ID: <strong>${orderId}</strong></p>
                        
                        <table style="width: 100%; border-collapse: collapse;">
                            ${itemsHtml}
                            <tr>
                                <td style="padding: 15px 0 0 0; font-weight: bold; color: #666;">Payment Method:</td>
                                <td style="padding: 15px 0 0 0; text-align: right; font-weight: bold; color: #666; font-size: 14px;">${isCOD ? 'Cash on Delivery' : (paymentMethod || 'Online Payment')}</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px 0 0 0; font-weight: bold; color: #2E7D32;">Total Amount:</td>
                                <td style="padding: 10px 0 0 0; text-align: right; font-weight: bold; color: #2E7D32; font-size: 18px;">Rs. ${totalPrice.toFixed(2)}</td>
                            </tr>
                        </table>
                    </div>
                    
                    <p style="color: #666; font-size: 14px;">If you have any questions about your order, please contact our support team.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px; text-align: center;">&copy; ${new Date().getFullYear()} Lanka Smart Mart. All rights reserved.</p>
                </div>
            `
        };

        await transporter.sendMail(mailOptions);

        // Send a distinct email to the admin
        const adminSubject = isCOD
            ? `New COD Order Alert - #${orderId.substring(0, 8).toUpperCase()}`
            : `New Paid Order Alert - #${orderId.substring(0, 8).toUpperCase()}`;

        const adminMailOptions = {
            from: `"Lanka Smart Mart App" <${process.env.SMTP_EMAIL}>`,
            to: '10nilupulthisaranga@gmail.com',
            subject: adminSubject,
            html: `
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                    <h2 style="color: #d32f2f; text-align: center;">New Order Alert!</h2>
                    <p style="color: #333; font-size: 16px;">A new order has been placed by <strong>${email}</strong>.</p>
                    <p style="color: #333; font-size: 14px; background-color: ${isCOD ? '#ffcdd2' : '#c8e6c9'}; padding: 10px; border-radius: 5px; text-align: center;">
                        <strong>Payment Type:</strong> ${isCOD ? 'Cash on Delivery (Payment Pending)' : 'Online Payment (Paid via ' + (paymentMethod || 'Card') + ')'}
                    </p>
                    
                    <div style="background-color: #fff3e0; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #333; margin-top: 0;">Order Details</h3>
                        <p style="color: #666; font-size: 14px; margin-bottom: 15px;">Order ID: <strong>${orderId}</strong></p>
                        
                        <table style="width: 100%; border-collapse: collapse;">
                            ${itemsHtml}
                            <tr>
                                <td style="padding: 15px 0 0 0; font-weight: bold; color: #d32f2f;">Total Amount:</td>
                                <td style="padding: 15px 0 0 0; text-align: right; font-weight: bold; color: #d32f2f; font-size: 18px;">Rs. ${totalPrice.toFixed(2)}</td>
                            </tr>
                        </table>
                    </div>
                </div>
            `
        };
        await transporter.sendMail(adminMailOptions);

        res.status(200).send({ success: true, message: 'Order confirmation emails sent successfully.' });
    } catch (error) {
        console.error("Error sending order email:", error);
        res.status(500).send({ success: false, error: error.message });
    }
});

app.post('/api/send-reset-email', async (req, res) => {
    const { email } = req.body;

    if (!email) {
        return res.status(400).send('Email is required');
    }

    try {
        // 1. Generate Password Reset Link using Firebase Admin
        const resetLink = await admin.auth().generatePasswordResetLink(email);

        // 2. Create the Custom HTML Email with Logo
        const mailOptions = {
            from: `"Lanka Smart Mart" <${process.env.SMTP_EMAIL}>`,
            to: email,
            subject: 'Password Reset - Lanka Smart Mart',
            html: `
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                    <div style="text-align: center; margin-bottom: 20px;">
                        <!-- Using actual logo from GitHub repository -->
                        <img src="https://raw.githubusercontent.com/N3Edirisinghe/Lanka_Smart_Mart/main/email-backend/logo.png" alt="Lanka Smart Mart Logo" style="max-width: 150px; height: auto;">
                    </div>
                    <h2 style="color: #2E7D32; text-align: center;">Reset Your Password</h2>
                    <p style="color: #333; font-size: 16px;">Hello,</p>
                    <p style="color: #333; font-size: 16px;">We received a request to reset your password for your Lanka Smart Mart account. Click the button below to reset it.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="${resetLink}" style="background-color: #2E7D32; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px;">Reset Password</a>
                    </div>
                    <p style="color: #666; font-size: 14px;">If you didn't request a password reset, you can safely ignore this email.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px; text-align: center;">&copy; ${new Date().getFullYear()} Lanka Smart Mart. All rights reserved.</p>
                </div>
            `
        };

        // 3. Send the Email
        await transporter.sendMail(mailOptions);

        res.status(200).send({ success: true, message: 'Password reset email sent successfully.' });

    } catch (error) {
        console.error("Error sending reset email:", error);
        res.status(500).send({ success: false, error: error.message });
    }
});

// Stripe Payment Intent Endpoint
app.post('/api/create-payment-intent', async (req, res) => {
    try {
        if (!process.env.STRIPE_SECRET_KEY) {
            return res.status(500).send({ error: 'Stripe Secret Key is missing from Server Environment configuration.' });
        }

        const stripe = Stripe(process.env.STRIPE_SECRET_KEY);
        const { amount, currency } = req.body;

        if (!amount || amount <= 0) {
            return res.status(400).send({ error: 'Invalid or missing amount' });
        }

        // Stripe requires a minimum charge equivalent to $0.50 USD.
        // For LKR, 150 LKR (15000 cents/smallest unit) is a safe minimum.
        // If the user's cart is under 150 Rs, we bump the intent to 15000 just to test it successfully.
        let finalAmount = parseInt(amount);
        if (finalAmount < 15000) {
            finalAmount = 15000;
        }

        // Create a PaymentIntent with the order amount and currency
        const paymentIntent = await stripe.paymentIntents.create({
            amount: finalAmount, // Amount in the smallest currency unit (e.g., cents for USD, or LKR cents)
            currency: currency || 'lkr',
            automatic_payment_methods: {
                enabled: true,
            },
        });

        res.status(200).send({
            clientSecret: paymentIntent.client_secret,
        });

    } catch (error) {
        console.error("Error creating payment intent:", error);
        res.status(500).send({ error: error.message });
    }
});

// For local testing
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));

module.exports = app;
