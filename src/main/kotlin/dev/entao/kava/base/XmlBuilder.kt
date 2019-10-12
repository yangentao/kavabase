@file:Suppress("MemberVisibilityCanBePrivate", "FunctionName", "unused")

package dev.entao.kava.base


import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class XmlNode(val element: Node) {

    fun node(name: String, vararg ps: Pair<String, Any>, block: XmlNode.() -> Unit): XmlNode {
        val e = element.ownerDocument.createElement(name)
        this.element.appendChild(e)
        val n = XmlNode(e)
        for (p in ps) {
            n.attr(p.first, p.second.toString())
        }
        n.block()
        return n
    }

    fun cdata(data: String) {
        val e = element.ownerDocument.createCDATASection(data)
        this.element.appendChild(e)
    }

    fun attr(key: String, value: String) {
        if (element is Element) {
            element.setAttribute(key, value)
        }
    }


    infix fun String.TO(value: Any) {
        attr(this, value.toString())
    }

    operator fun String?.unaryPlus() {
        text(this)
    }

    fun text(text: String?) {
        val s = text ?: return
        val node = element.ownerDocument.createTextNode(s)
        element.appendChild(node)
    }

    fun text(block: () -> String?) {
        text(block())
    }

    override fun toString(): String {
        return this.toXml(false, false)
    }

    fun toXml(xmlDeclare: Boolean, indent: Boolean): String {
        val t = TransformerFactory.newInstance().newTransformer()
        if (!xmlDeclare) {
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        }
        if (indent) {
            t.setOutputProperty(OutputKeys.INDENT, "yes")
        }
        val w = StringWriter(2048)
        t.transform(DOMSource(this.element), StreamResult(w))
        return w.toString()
    }

}

fun xmlRoot(rootName: String, vararg ps: Pair<String, Any>, block: XmlNode.() -> Unit): XmlNode {
    val fac = DocumentBuilderFactory.newInstance()
    val db = fac.newDocumentBuilder()
    val doc = db.newDocument()
    val e = doc.createElement(rootName)
    doc.appendChild(e)
    val n = XmlNode(e)
    for (p in ps) {
        n.attr(p.first, p.second.toString())
    }
    n.block()
    return n
}

fun testXml() {
    val a = xmlRoot("Person", "age" to 38) {
        node("child", "name" to "suo", "age" to 9) {
            node("school") {
                cdata("WenYuan")
            }
        }
    }
    print(a.toXml(true, true))
}

fun main() {
    testXml()
}

