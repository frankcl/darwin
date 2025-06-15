package xin.manong.darwin.service.lineage;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 血统节点
 *
 * @author frankcl
 * @date 2025-06-14 11:40:15
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Node {

    @JSONField(name = "key")
    @JsonProperty("key")
    private String key;
    @JSONField(name = "parent_key")
    @JsonProperty("parent_key")
    private String parentKey;
    @JSONField(name = "url")
    @JsonProperty("url")
    private String url;
    @JSONField(name = "parent")
    @JsonProperty("parent")
    private Node parent;
    @JSONField(name = "children")
    @JsonProperty("children")
    private final List<Node> children;

    public Node(String key, String url) {
        this.key = key;
        this.url = url;
        this.children = new ArrayList<>();
    }

    /**
     * 增加节点
     *
     * @param child 孩子节点
     * @return 节点自身
     */
    public Node addChild(Node child) {
        children.add(child);
        return this;
    }

    /**
     * 删除节点
     *
     * @param child 孩子节点
     * @return 删除成功返回true，否则返回false
     */
    public boolean removeChild(Node child) {
        return children.remove(child);
    }
}
