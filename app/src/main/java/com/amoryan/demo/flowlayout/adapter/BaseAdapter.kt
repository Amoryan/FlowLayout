package com.amoryan.demo.flowlayout.adapter

/**
 * @author: Domino
 * @className: BaseAdapter
 * @description:
 * @createTime: 2017/9/14 下午8:01
 */
abstract class BaseAdapter<T>(var data: ArrayList<T>) {

    abstract fun getCount(): Int

}