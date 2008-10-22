/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.dataquality.indicators.definition;

import org.eclipse.emf.common.util.EList;

import orgomg.cwm.objectmodel.core.Expression;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Indicator Definition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.dataquality.indicators.definition.IndicatorDefinition#getCategories <em>Categories</em>}</li>
 *   <li>{@link org.talend.dataquality.indicators.definition.IndicatorDefinition#getAggregatedDefinitions <em>Aggregated Definitions</em>}</li>
 *   <li>{@link org.talend.dataquality.indicators.definition.IndicatorDefinition#getLabel <em>Label</em>}</li>
 *   <li>{@link org.talend.dataquality.indicators.definition.IndicatorDefinition#getSubCategories <em>Sub Categories</em>}</li>
 *   <li>{@link org.talend.dataquality.indicators.definition.IndicatorDefinition#getSqlGenericExpression <em>Sql Generic Expression</em>}</li>
 *   <li>{@link org.talend.dataquality.indicators.definition.IndicatorDefinition#getNumeric1argFunctions <em>Numeric1arg Functions</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.dataquality.indicators.definition.DefinitionPackage#getIndicatorDefinition()
 * @model
 * @generated
 */
public interface IndicatorDefinition extends ModelElement {
    /**
     * Returns the value of the '<em><b>Categories</b></em>' reference list.
     * The list contents are of type {@link org.talend.dataquality.indicators.definition.IndicatorCategory}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Categories</em>' reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Categories</em>' reference list.
     * @see org.talend.dataquality.indicators.definition.DefinitionPackage#getIndicatorDefinition_Categories()
     * @model
     * @generated
     */
    EList<IndicatorCategory> getCategories();

    /**
     * Returns the value of the '<em><b>Aggregated Definitions</b></em>' reference list.
     * The list contents are of type {@link org.talend.dataquality.indicators.definition.IndicatorDefinition}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Aggregated Definitions</em>' reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Aggregated Definitions</em>' reference list.
     * @see org.talend.dataquality.indicators.definition.DefinitionPackage#getIndicatorDefinition_AggregatedDefinitions()
     * @model
     * @generated
     */
    EList<IndicatorDefinition> getAggregatedDefinitions();

    /**
     * Returns the value of the '<em><b>Label</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Label</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Label</em>' attribute.
     * @see #setLabel(String)
     * @see org.talend.dataquality.indicators.definition.DefinitionPackage#getIndicatorDefinition_Label()
     * @model id="true"
     * @generated
     */
    String getLabel();

    /**
     * Sets the value of the '{@link org.talend.dataquality.indicators.definition.IndicatorDefinition#getLabel <em>Label</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Label</em>' attribute.
     * @see #getLabel()
     * @generated
     */
    void setLabel(String value);

    /**
     * Returns the value of the '<em><b>Sub Categories</b></em>' reference list.
     * The list contents are of type {@link org.talend.dataquality.indicators.definition.IndicatorCategory}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Sub Categories</em>' reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Sub Categories</em>' reference list.
     * @see org.talend.dataquality.indicators.definition.DefinitionPackage#getIndicatorDefinition_SubCategories()
     * @model
     * @generated
     */
    EList<IndicatorCategory> getSubCategories();

    /**
     * Returns the value of the '<em><b>Sql Generic Expression</b></em>' containment reference list.
     * The list contents are of type {@link orgomg.cwm.objectmodel.core.Expression}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Sql Generic Expression</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Sql Generic Expression</em>' containment reference list.
     * @see org.talend.dataquality.indicators.definition.DefinitionPackage#getIndicatorDefinition_SqlGenericExpression()
     * @model containment="true"
     * @generated
     */
    EList<Expression> getSqlGenericExpression();

    /**
     * Returns the value of the '<em><b>Numeric1arg Functions</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * list of numeric functions to be used by this indicator definition (e.g. AVG({0)}, SUM({0}), COUNT({0})...)
     * 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Numeric1arg Functions</em>' attribute list.
     * @see org.talend.dataquality.indicators.definition.DefinitionPackage#getIndicatorDefinition_Numeric1argFunctions()
     * @model
     * @generated
     */
    EList<String> getNumeric1argFunctions();

} // IndicatorDefinition
