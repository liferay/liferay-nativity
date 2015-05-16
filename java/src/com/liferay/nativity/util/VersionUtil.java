package com.liferay.nativity.util;

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

import java.util.regex.Pattern;

/**
 * @author Dennis Ju
 */
public class VersionUtil {

	public static int compare(String version1, String version2) {
		int componentCount1 =
			version1.length() - version1.replace(".", "").length();
		int componentCount2 =
			version2.length() - version2.replace(".", "").length();

		if (componentCount1 > componentCount2) {
			int difference = componentCount1 - componentCount2;

			for (int i = 0; i < difference; i++) {
				version2 = version2 + ".0";
			}
		}
		else if (componentCount2 > componentCount1) {
			int difference = componentCount2 - componentCount1;

			for (int i = 0; i < difference; i++) {
				version1 = version1 + ".0";
			}
		}

		String normalizedVersion1 = _normalizedVersion(version1);
		String normalizedVersion2 = _normalizedVersion(version2);

		return normalizedVersion1.compareTo(normalizedVersion2);
	}

	private static String _normalizedVersion(String version) {
		String[] splitVersion = _pattern.split(version);

		StringBuilder sb = new StringBuilder();

		for (String versionComponent : splitVersion) {
			sb.append(String.format("%" + 4 + 's', versionComponent));
		}

		return sb.toString();
	}

	private static Pattern _pattern = Pattern.compile(".", Pattern.LITERAL);

}