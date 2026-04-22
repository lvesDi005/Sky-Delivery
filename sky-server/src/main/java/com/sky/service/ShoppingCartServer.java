package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.PageResult;

import java.util.List;

/**
 * @Description ShoppingCartServer
 * @Author kight-tom
 * @Date 2026-04-22  9:06
 */
public interface ShoppingCartServer {

    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();

    void clean();

    void sub(ShoppingCartDTO shoppingCartDTO);
}
