package com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.changer;




import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.JdbcTypeUtils;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.MxParameterChanger;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.iftest.ConditionFieldWrapper;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxParameter;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The type In parameter changer.
 */
public class InParameterChanger implements MxParameterChanger {
    @Override
    public List<TxParameter> getParameter(TxParameter txParameter) {
        TxParameter collectionParameter = TxParameter.createByOrigin(txParameter.getName() + "List",
                "Collection<" + txParameter.getTypeText() + ">",
                "java.util.Collection");
        return Arrays.asList(collectionParameter);
    }

    @Override
    public String getTemplateText(String fieldName, LinkedList<PsiParameter> parameters, ConditionFieldWrapper conditionFieldWrapper) {
        final PsiParameter collection = parameters.poll();
        final String collectionName = collection.getName();
        String itemContent = "#{item}";
        // 如果集合的泛型不是空的, 就给遍历的内容加入 jdbcType
        if (collection.getTypeElement() != null) {
            PsiJavaCodeReferenceElement innermostComponentReferenceElement = collection.getTypeElement().getInnermostComponentReferenceElement();
            if(innermostComponentReferenceElement!=null){
                final @NotNull PsiType[] typeParameters = innermostComponentReferenceElement.getTypeParameters();
                if (typeParameters.length > 0) {
                    final PsiType typeParameter = typeParameters[0];
                    itemContent = JdbcTypeUtils.wrapperField("item", typeParameter.getCanonicalText());
                }
            }

        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fieldName).append(" ").append(getIn()).append("\n");
        stringBuilder.append("<foreach collection=\"").append(collectionName).append("\" item=\"item\" open=\"(\" close=\")\" separator=\",\">").append("\n");
        stringBuilder.append(itemContent).append("\n");
        stringBuilder.append("</foreach>");
        return stringBuilder.toString();
    }

    /**
     * Gets in.
     *
     * @return the in
     */
    @NotNull
    protected String getIn() {
        return "in";
    }


}
