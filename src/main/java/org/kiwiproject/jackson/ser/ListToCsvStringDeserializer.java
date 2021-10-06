package org.kiwiproject.jackson.ser;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Custom Jackson {@link JsonDeserializer} to examine a node and take different action based on whether it is
 * a {@link TextNode} or a container node. If it is a TextNode, return the node's text, otherwise collect the
 * values in the container and join them into a CSV string.
 * <p>
 * This implementation does not perform extensive error checking; it assumes the element being deserialized
 * contains either a TextNode or is a container of TextNode. If a TextNode, it assumes the content is CSV but does
 * not verify. For example, as a TextNode in YAML:
 *
 * <pre>
 * someProperty: value1,value2,value3
 * </pre>
 * <p>
 * And as a container node:
 *
 * <pre>
 * someProperty:
 *   - value1
 *   - value2
 *   - value3
 * </pre>
 * <p>
 * Both of the above YAML configurations will result in the value "value1,value2,value3".
 *
 * @implNote The deserialization requires {@link JsonParser#getCodec()} to return a non-null codec and will throw an
 * {@link IllegalStateException} if the returned codec is null.
 */
public class ListToCsvStringDeserializer extends StdDeserializer<String> {

    @SuppressWarnings("WeakerAccess")
    public ListToCsvStringDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        var codec = parser.getCodec();
        checkState(nonNull(codec),
                "There is no codec associated with the parser; a codec is required to read the content as a tree");

        var treeNode = codec.readTree(parser);

        if (treeNode.isContainerNode()) {
            return containerNodeToCsvString(treeNode);
        }

        return verifyIsTextNode(treeNode).asText();
    }

    private static String containerNodeToCsvString(TreeNode treeNode) {
        return IntStream.range(0, treeNode.size())
                .mapToObj(index -> textNodeToString(treeNode, index))
                .collect(joining(","));
    }

    private static String textNodeToString(TreeNode treeNode, int index) {
        var node = treeNode.get(index);
        return verifyIsTextNode(node).asText();
    }

    private static TextNode verifyIsTextNode(TreeNode node) {
        verify(node instanceof TextNode, "expected node to be TextNode but was: %s", node.getClass().getName());
        return (TextNode) node;
    }
}
