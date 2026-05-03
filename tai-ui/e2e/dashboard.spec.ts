// SPDX-License-Identifier: GPL-3.0-only
import { expect, test } from '@playwright/test';

test('renders Tai dashboard shell', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Tai' })).toBeVisible();
  await expect(page.getByText('System & Module Health')).toBeVisible();
  await expect(page.getByText('Avatar Render')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Send' })).toBeVisible();
});
