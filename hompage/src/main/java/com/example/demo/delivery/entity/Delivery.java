package com.example.demo.delivery.entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {
	private Long dno;
	private String memberId;
	private Long zipCode;
	private String receiverName;
	private String deliveryAddress;
	private String secondAddress;
	private String thirdAddress;
	private String receiverTel;
}
