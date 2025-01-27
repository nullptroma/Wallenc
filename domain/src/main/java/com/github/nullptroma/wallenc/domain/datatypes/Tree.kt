package com.github.nullptroma.wallenc.domain.datatypes

class Tree<T>(val value: T, var children: List<Tree<T>>? = null)