/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.nativity.util;

import java.text.Normalizer;

/**
 * @author Dennis Ju
 */
public class StringUtil {

	public static String normalize(String text) {
		String[] texts = normalize(new String[] { text });

		return texts[0];
	}

	public static String[] normalize(String[] texts) {
		String[] normalizedTexts = new String[texts.length];

		for (int i = 0; i < texts.length; i++) {
			if (texts[i] == null) {
				continue;
			}

			normalizedTexts[i] = Normalizer.normalize(
				texts[i], Normalizer.Form.NFC);
		}

		return normalizedTexts;
	}

}