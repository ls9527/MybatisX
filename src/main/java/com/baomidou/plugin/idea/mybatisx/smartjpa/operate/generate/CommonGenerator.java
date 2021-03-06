package com.baomidou.plugin.idea.mybatisx.smartjpa.operate.generate;

import com.baomidou.plugin.idea.mybatisx.smartjpa.common.MapperClassGenerateFactory;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.SyntaxAppender;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.AreaSequence;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.CustomFieldAppender;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.iftest.ConditionFieldWrapper;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxField;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxParameter;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxParameterDescriptor;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TypeDescriptor;
import com.baomidou.plugin.idea.mybatisx.smartjpa.db.adaptor.DasTableAdaptor;
import com.baomidou.plugin.idea.mybatisx.smartjpa.db.adaptor.DbmsAdaptor;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.manager.AreaOperateManager;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.manager.AreaOperateManagerFactory;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.model.AppendTypeEnum;
import com.intellij.openapi.application.WriteAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 常用的生成器
 */
public class CommonGenerator implements PlatformGenerator {
    private final String defaultDateWord;
    private @NotNull LinkedList<SyntaxAppender> jpaList;
    private List<TxField> mappingField;
    private String tableName;
    private PsiClass entityClass;
    /**
     * The Appender manager.
     */
    final AreaOperateManager appenderManager;
    private String text;


    private CommonGenerator(PsiClass entityClass,
                            String text,
                            DbmsAdaptor dbms,
                            DasTableAdaptor dasTable,
                            String tableName,
                            List<TxField> fields) {
        this.entityClass = entityClass;
        this.text = text;
        mappingField = fields;
        defaultDateWord = dbms.getDefaultDateWord();
        this.tableName = tableName;

        appenderManager = AreaOperateManagerFactory.getAreaOperateManagerByDbms(dbms, mappingField, entityClass, dasTable, this.tableName);
        jpaList = appenderManager.splitAppenderByText(text);
    }

    @Override
    public String getDefaultDateWord() {
        return defaultDateWord;
    }

    /**
     * Create editor auto completion common generator.
     *
     * @param entityClass the entity class
     * @param text        the text
     * @param dbms        the dbms
     * @param dasTable    the das table
     * @param tableName   the table name
     * @param fields      the fields
     * @return common generator
     */
    public static CommonGenerator createEditorAutoCompletion(PsiClass entityClass, String text,
                                                             @NotNull DbmsAdaptor dbms,
                                                             DasTableAdaptor dasTable,
                                                             String tableName,
                                                             List<TxField> fields) {
        return new CommonGenerator(entityClass, text, dbms, dasTable, tableName, fields);
    }


    @Override
    public TypeDescriptor getParameter() {
        List<TxParameter> parameters = appenderManager.getParameters(entityClass, new LinkedList<>(jpaList));
        return new TxParameterDescriptor(parameters, mappingField);
    }


    @Override
    public TypeDescriptor getReturn() {
        LinkedList<SyntaxAppender> linkedList = new LinkedList<>(jpaList);
        return appenderManager.getReturnWrapper(text, entityClass, linkedList);
    }

    @Override
    public void generateMapperXml(MapperClassGenerateFactory mapperClassGenerateFactory,
                                  PsiMethod psiMethod,
                                  ConditionFieldWrapper conditionFieldWrapper) {
        WriteAction.run(()->{
            // 生成完整版的内容
            Generator generator = conditionFieldWrapper.getGenerator(mapperClassGenerateFactory);
            appenderManager.generateMapperXml(
                text,
                new LinkedList<>(jpaList),
                entityClass,
                psiMethod,
                tableName,
                generator,
                conditionFieldWrapper);
        });
    }

    @Override
    public List<String> getConditionFields() {
        return jpaList.stream()
            .filter(syntaxAppender -> syntaxAppender.getAreaSequence() == AreaSequence.CONDITION
                && syntaxAppender.getType() == AppendTypeEnum.FIELD &&
                syntaxAppender instanceof CustomFieldAppender)
            .map(x -> ((CustomFieldAppender) x).getFieldName())
            .collect(Collectors.toList());
    }

    @Override
    public List<TxField> getAllFields() {
        return mappingField;
    }

    @Override
    public String getEntityClass() {
        return entityClass.getQualifiedName();
    }
}
