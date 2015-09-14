/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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
#ifndef __CONTENT_H__
#define __CONTENT_H__

#include <map>
#include <set>
#include <string>

class ContentManager
{
	public:
		static ContentManager& instance();

		std::string getFileIconName(const std::string& fileName) const;
		void setFileIcon(const std::string& fileName, int icon);
		void removeFileIcon(const std::string& fileName);
		void removeAllFileIcons();
		int registerIcon(const std::string& fileName);
		void registerIconWithId(const std::string& fileName, const std::string& iconId);
		void unregisterIcon(int iconId);
		void enableFileIcons(bool enable);
		bool isOverlaysEnabled();
		void setRootFolder(std::string const& rootFolder);
		const std::string& getRootFolder() const;

	private:
		std::map<std::string, std::string> iconsForFiles_;
		int lastIconId_;
		std::map<std::string, std::string> icons_;
		bool overlaysEnabled_;
		std::string rootFolder_;
		mutable std::set<std::string> registeredFolders_;

		ContentManager();
		~ContentManager();
};

#endif