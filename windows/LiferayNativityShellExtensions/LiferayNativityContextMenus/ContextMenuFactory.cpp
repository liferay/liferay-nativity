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

#include "ContextMenuFactory.h"

extern long dllReferenceCount;

ContextMenuFactory::ContextMenuFactory(wchar_t* modulePath) : _modulePath(modulePath), _referenceCount(1)
{
	InterlockedIncrement(&dllReferenceCount);
}

ContextMenuFactory::~ContextMenuFactory()
{
	InterlockedDecrement(&dllReferenceCount);
}

IFACEMETHODIMP ContextMenuFactory::QueryInterface(REFIID riid, void** ppv)
{
	HRESULT hResult = S_OK;

	if (IsEqualIID(IID_IUnknown, riid) ||
	        IsEqualIID(IID_IClassFactory, riid))
	{
		*ppv = static_cast<IUnknown*>(this);

		AddRef();
	}
	else
	{
		hResult = E_NOINTERFACE;

		*ppv = NULL;
	}

	return hResult;
}

IFACEMETHODIMP_(ULONG) ContextMenuFactory::AddRef()
{
	long result = InterlockedIncrement(&_referenceCount);

	return result;
}

IFACEMETHODIMP_(ULONG) ContextMenuFactory::Release()
{
	long cRef = InterlockedDecrement(&_referenceCount);

	if (0 == cRef)
	{
		delete this;
	}

	return cRef;
}

IFACEMETHODIMP ContextMenuFactory::CreateInstance(
    IUnknown* pUnkOuter, REFIID riid, void** ppv)
{
	HRESULT hResult = CLASS_E_NOAGGREGATION;

	if (pUnkOuter != NULL)
	{
		return hResult;
	}

	hResult = E_OUTOFMEMORY;

	LiferayNativityContextMenus* liferayNativityContextMenus = new(std::nothrow) LiferayNativityContextMenus();

	if (!liferayNativityContextMenus)
	{
		return hResult;
	}

	hResult = liferayNativityContextMenus->QueryInterface(riid, ppv);

	liferayNativityContextMenus->Release();

	return hResult;
}

IFACEMETHODIMP ContextMenuFactory::LockServer(BOOL fLock)
{
	if (fLock)
	{
		InterlockedIncrement(&dllReferenceCount);
	}
	else
	{
		InterlockedDecrement(&dllReferenceCount);
	}
	return S_OK;
}