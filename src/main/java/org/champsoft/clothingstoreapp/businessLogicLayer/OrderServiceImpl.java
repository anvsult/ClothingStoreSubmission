package org.champsoft.clothingstoreapp.businessLogicLayer;

import org.champsoft.clothingstoreapp.dataAccessLayer.*;
import org.champsoft.clothingstoreapp.dataMapperLayer.OrderRequestMapper;
import org.champsoft.clothingstoreapp.dataMapperLayer.OrderResponseMapper;
import org.champsoft.clothingstoreapp.presentationLayer.CustomerResponseModel;
import org.champsoft.clothingstoreapp.presentationLayer.OrderRequestModel;
import org.champsoft.clothingstoreapp.presentationLayer.OrderResponseModel;
import org.champsoft.clothingstoreapp.presentationLayer.ProductResponseModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderResponseMapper orderResponseMapper;
    private final OrderRequestMapper orderRequestMapper;

    private final CustomerService customerService;
    private final ProductService productService;

    public OrderServiceImpl(OrderRepository orderRepository, OrderResponseMapper orderResponseMapper, OrderRequestMapper orderRequestMapper, CustomerService customerService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderResponseMapper = orderResponseMapper;
        this.orderRequestMapper = orderRequestMapper;
        this.customerService = customerService;
        this.productService = productService;
    }

    @Override
    public List<OrderResponseModel> getOrders() {
        List<Order> orders = this.orderRepository.findAll();
        List<OrderResponseModel> orderResponseModels = new ArrayList<>();
        for (Order order: orders) {
            String customerId = order.getCustomerIdentifier();
            String productId = order.getProductIdentifier();
            CustomerResponseModel customerWhoBought = customerService.getCustomerById(customerId);
            ProductResponseModel productBought = productService.getProductById(productId);
            OrderResponseModel orm = orderResponseMapper.entityToResponseModel(order, customerWhoBought, productBought);

            orderResponseModels.add(orm);
        }
        return orderResponseModels;
    }

    @Override
    public OrderResponseModel getOrderById(String orderId) {
        Order order = orderRepository.findOrderByOrderIdentifier(orderId);
        return orderResponseMapper.entityToResponseModel(order, customerService.getCustomerById(order.getCustomerIdentifier()), productService.getProductById(order.getProductIdentifier()));
    }

    @Override
    public String addOrder(OrderRequestModel newOrderData) {
        String message = "";
        Order order = orderRequestMapper.requestModelToEntity(newOrderData);
        CustomerResponseModel foundCustomer = customerService.getCustomerById(order.getCustomerIdentifier());
        ProductResponseModel foundProduct = productService.getProductById(order.getProductIdentifier());

        if (foundCustomer == null) {
            message = "Customer with " + order.getCustomerIdentifier() + " not found";
        }
        if (foundProduct == null) {
            return "Product with " + order.getProductIdentifier() + " not found";
        }
        order.setName(foundProduct.getName());
        order.setPrice(foundProduct.getPrice());
        order.setTotalPrice(order.getPrice().add(order.getShippingPrice()));
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        orderRepository.save(order);
        message = "Order with id : " + order.getOrderIdentifier() + " added successfully";
        return message;
    }

    @Override
    public String updateOrder(String orderId, OrderRequestModel newOrderData) {
        String message = "";
        Order foundOrder = orderRepository.findOrderByOrderIdentifier(orderId);
        if (foundOrder == null){
            message = "Order with " + orderId + " not found";
        } else {
            Order order = orderRequestMapper.requestModelToEntity(newOrderData);
            order.setOrderIdentifier(orderId);
            order.setId(foundOrder.getId());

            order.setName(foundOrder.getName());
            order.setPrice(foundOrder.getPrice());
            order.setDeliveryStatus(foundOrder.getDeliveryStatus());
            order.setTotalPrice(order.getPrice().add(order.getShippingPrice()));

            this.orderRepository.save(order);
            message = "Order with id : " + orderId + " updated successfully";
        }
        return message;
    }

    @Override
    public String deleteOrderByOrderId(String orderId) {
        String message = "";
        Order foundOrder = orderRepository.findOrderByOrderIdentifier(orderId);
        if (foundOrder == null){
            message = "Order with " + orderId + " not found";
        } else {
            orderRepository.delete(foundOrder);
            message = "Order with id : " + orderId + " deleted successfully";
        }
        return message;
    }

    @Override
    public List<OrderResponseModel> getOrdersByCustomerId(String customerId) {
        List<Order> orders = orderRepository.findOrdersByCustomerIdentifier(customerId);
        List<OrderResponseModel> orderResponseModels = new ArrayList<>();
        for (Order order: orders) {
            String productId = order.getProductIdentifier();
            CustomerResponseModel customerWhoBought = customerService.getCustomerById(customerId);
            ProductResponseModel productBought = productService.getProductById(productId);
            OrderResponseModel orm = orderResponseMapper.entityToResponseModel(order, customerWhoBought, productBought);

            orderResponseModels.add(orm);
        }
        return orderResponseModels;
    }

    @Override
    public List<OrderResponseModel> getOrdersByProductId(String productId) {
        List<Order> orders = orderRepository.findOrdersByProductIdentifier(productId);
        List<OrderResponseModel> orderResponseModels = new ArrayList<>();
        for (Order order: orders) {
            String customerId = order.getCustomerIdentifier();
            CustomerResponseModel customerWhoBought = customerService.getCustomerById(customerId);
            ProductResponseModel productBought = productService.getProductById(productId);
            OrderResponseModel orm = orderResponseMapper.entityToResponseModel(order, customerWhoBought, productBought);

            orderResponseModels.add(orm);
        }
        return orderResponseModels;
    }
}
