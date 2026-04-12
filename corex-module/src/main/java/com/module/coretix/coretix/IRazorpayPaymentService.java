package com.module.coretix.coretix;

import com.module.coretix.commonto.RazorpayOrderRequestTO;
import com.module.coretix.commonto.RazorpayOrderResultTO;

public interface IRazorpayPaymentService {

    RazorpayOrderResultTO createOrder(RazorpayOrderRequestTO request);

    boolean verifySignature(String orderId, String paymentId, String signature);

    boolean isConfigured();
}
