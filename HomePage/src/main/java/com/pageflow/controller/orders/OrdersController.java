package com.pageflow.controller.orders;

import java.security.*;
import java.util.*;

import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import com.pageflow.dao.delivery.*;
import com.pageflow.dto.cart.*;
import com.pageflow.dto.delivery.*;
import com.pageflow.dto.member.*;
import com.pageflow.dto.orders.*;
import com.pageflow.entity.delivery.*;
import com.pageflow.entity.orderDetails.*;
import com.pageflow.entity.orders.*;
import com.pageflow.service.delivery.*;
import com.pageflow.service.member.*;
import com.pageflow.service.orderDetails.*;
import com.pageflow.service.orders.*;

@Secured("ROLE_USER")
@Controller
public class OrdersController {
  @Autowired
  private DeliveryService deliveryService;

  @Autowired
  private MemberService memberService;

  @Autowired
  private DeliveryDao deliveryDao;

  @Autowired
  private OrdersService ordersService;

  @Autowired
  private OrderDetailsService orderDetailsService;

  // 1. 주문 페이지 보여주기
  @GetMapping("/order")
  public ModelAndView order(HttpServletRequest request, Principal principal, Long dno) {
    HttpSession session = request.getSession();

    @SuppressWarnings("unchecked")
    List<CartDto.Select> cartList = (List<CartDto.Select>) session.getAttribute("cartList");
    DeliveryDto.Read delivery = (DeliveryDto.Read) session.getAttribute("delivery");
    MemberDto.Read memberId = memberService.read(principal.getName());

    Long point = memberService.findPoint(principal.getName());

    return new ModelAndView("order_page").addObject("cartList", cartList).addObject("point", point)
        .addObject("delivery", delivery).addObject("memberId", memberId);
  }

  // 1-2. 주문하기
  @PostMapping("/order")
  @ResponseBody
  public ResponseEntity<Orders> order(@RequestBody Orders order, Principal principal, HttpSession session) {

    @SuppressWarnings("unchecked")
    List<CartDto.Select> cartList = (List<CartDto.Select>) session.getAttribute("cartList");
    DeliveryDto.Read delivery = deliveryService.read(order.getDno(), principal.getName());
    session.setAttribute("delivery", delivery);
    Boolean result = ordersService.add(order, principal.getName(), cartList);

    if (!result) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    return ResponseEntity.ok(order);
  }

  // 1-3 주문 페이지에서 배송지 목록 페이지 보여주기
  @GetMapping("/order/delivery/list")
  public ModelAndView deliveryList(@RequestParam(defaultValue = "1") Long pageno, Principal principal) {
    DeliveryPage page = deliveryService.list(pageno, principal.getName());
    return new ModelAndView("order_delivery_page").addObject("page", page);
  }

  // 1-4 주문 페이지에서 배송지 선택해주기
  @PostMapping("order/delivery/list")
  public ModelAndView deliveryRead(Long dno, Principal principal, HttpSession session) {
    DeliveryDto.Read delivery = deliveryService.read(dno, principal.getName());
    session.setAttribute("delivery", delivery);
    return new ModelAndView("redirect:/order");
  }

  // 1-5. 주문 페이지에서 배송지 목록 추가하기
  @PostMapping("/order/delivery/add")
  public ModelAndView deliveryAdd(Principal principal, DeliveryDto.Create dto) {
    deliveryService.deliveryAdd(principal.getName(), dto);
    return new ModelAndView("redirect:/order/delivery/list");
  }

  // 1-6. 주문 페이지에서 배송지 수정 페이지
  @GetMapping("/order/delivery/update/{dno}")
  public ModelAndView update(@PathVariable Long dno, Principal principal, DeliveryDto.Update dto) {
    Delivery delivery = deliveryDao.read(dno, principal.getName());
    return new ModelAndView("order_delivery_update_page").addObject("delivery", delivery);
  }

  // 1-7. 배송지 수정
  @PostMapping("/order/delivery/update/{dno}")
  public ModelAndView update(@PathVariable Long dno, DeliveryDto.Update dto, Principal principal) {
    deliveryService.update(dto, principal.getName());
    return new ModelAndView("redirect:/order/delivery/list");
  }

  // 1-8. 배송지 삭제
  @PostMapping("/order/delivery/delete")
  public ModelAndView delete(Long dno) {
    deliveryService.delete(dno);
    return new ModelAndView("redirect:/order/delivery/list");
  }

  // 1-9. 기본 배송지 변경
  @PostMapping("/order/defaultAddress/change")
  public ModelAndView change(Long dno, Principal principal, HttpSession session) {
    deliveryService.change(dno, principal.getName());
    DeliveryDto.Read delivery = deliveryService.read(dno, principal.getName());
    session.setAttribute("delivery", delivery);
    return new ModelAndView("redirect:/order");
  }

  // 1-10. 배송 메세지 페이지
  @GetMapping("/order/delivery/message/{dno}")
  public ModelAndView message(@PathVariable Long dno, Principal principal, DeliveryDto.Message dto) {
    Delivery delivery = deliveryDao.read(dno, principal.getName());
    return new ModelAndView("order_message_update_page").addObject("delivery", delivery);
  }

  // 1-11. 배송 메세지 변경
  @PostMapping("/order/delivery/message/{dno}")
  public ModelAndView message(@PathVariable Long dno, DeliveryDto.Message dto, Principal principal,
      HttpSession session) {
    deliveryService.changeMessage(dto, principal.getName());
    DeliveryDto.Read delivery = deliveryService.read(dno, principal.getName());
    session.setAttribute("delivery", delivery);
    return new ModelAndView("redirect:/order");
  }

  // 2. 주문 성공 페이지
  @GetMapping("order/success")
  public ModelAndView orderSuccess(@RequestParam Long ono, HttpSession session) {
    List<OrderDetails> list = orderDetailsService.list(ono);
    Orders order = ordersService.findOrder(ono);
    DeliveryDto.Read delivery = (DeliveryDto.Read) session.getAttribute("delivery");
    return new ModelAndView("order_success_page").addObject("list", list).addObject("delivery", delivery)
        .addObject("order", order);
  }

  // 3. 주문 목록 페이지
  @GetMapping("/order/list")
  public ModelAndView orderList(Principal principal) {
    List<OrdersDto.OrdersList> orderList = ordersService.getOrdersList(principal.getName());
    return new ModelAndView("order_list_page").addObject("orderList", orderList);
  }

  // 4. 주문 읽기
  @GetMapping("/order/read")
  public ModelAndView read(@RequestParam(required = false) Long ono, Principal principal) {
    OrdersDto.Read read = ordersService.readOrders(ono, principal.getName());
    System.out.println(read);
    return new ModelAndView("order_detail_list_page").addObject("read", read);
  }

}
