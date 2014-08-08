/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

#ifndef CONTEXTMENUFACTORY_H
#define CONTEXTMENUFACTORY_H

#pragma once

#include "LiferayNativityContextMenus.h"
#include "stdafx.h"

class ContextMenuFactory : public IClassFactory
{
	public:
		ContextMenuFactory(wchar_t*);

		IFACEMETHODIMP_(ULONG) AddRef();

		IFACEMETHODIMP CreateInstance(IUnknown* pUnkOuter, REFIID riid, void** ppv);

		IFACEMETHODIMP LockServer(BOOL fLock);

		IFACEMETHODIMP QueryInterface(REFIID riid, void** ppv);

		IFACEMETHODIMP_(ULONG) Release();

	protected:
		~ContextMenuFactory();

	private:
		long _referenceCount;
		wchar_t* _modulePath;
};

#endif