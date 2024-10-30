package dev.littlexfish.dto

data class DirectoryStruct(val name: String, val nodes: List<Node>)

sealed class Node(val name: String) {
	class Leaf(name: String): Node(name)
	class Branch(name: String, var children: List<Node>): Node(name)
}

