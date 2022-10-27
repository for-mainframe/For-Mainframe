/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */
package eu.ibagroup.formainframe.utils.crudable.annotations

import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Interface to describe the element that contains the entries, provided by the "entities" method
 */
@Target(
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Inherited
annotation class Contains(val entities: Array<KClass<*>>)
