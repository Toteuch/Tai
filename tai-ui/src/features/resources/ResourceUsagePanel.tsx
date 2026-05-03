// SPDX-License-Identifier: GPL-3.0-only
import type { ComponentType } from 'react';
import { BrainCircuit, Cpu, Mic, Network, Volume2, Waves } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ResourceKind, useLocalUiStore } from '@/store/localUiStore';

const mockedUsage: Record<ResourceKind, Array<{ name: string; value: number; icon: ComponentType<{ className?: string }> }>> = {
  CPU: [
    { name: 'Orchestrator', value: 23, icon: Network },
    { name: 'STT Listener', value: 18, icon: Waves },
    { name: 'STT Whisper', value: 41, icon: Mic },
    { name: 'LLM', value: 72, icon: BrainCircuit },
    { name: 'TTS Piper', value: 27, icon: Volume2 },
  ],
  GPU: [
    { name: 'Orchestrator', value: 3, icon: Network },
    { name: 'STT Listener', value: 2, icon: Waves },
    { name: 'STT Whisper', value: 44, icon: Mic },
    { name: 'LLM', value: 78, icon: BrainCircuit },
    { name: 'TTS Piper', value: 6, icon: Volume2 },
  ],
  RAM: [
    { name: 'Orchestrator', value: 28, icon: Network },
    { name: 'STT Listener', value: 15, icon: Waves },
    { name: 'STT Whisper', value: 34, icon: Mic },
    { name: 'LLM', value: 61, icon: BrainCircuit },
    { name: 'TTS Piper', value: 20, icon: Volume2 },
  ],
};

export function ResourceUsagePanel() {
  const selectedResource = useLocalUiStore((state) => state.selectedResource);
  const setSelectedResource = useLocalUiStore((state) => state.setSelectedResource);
  const items = mockedUsage[selectedResource];

  return (
    <Card className="min-h-0 overflow-hidden">
      <CardHeader className="flex-row justify-between py-3">
        <div className="flex items-center gap-3">
          <Cpu className="h-5 w-5 text-sky-300" />
          <CardTitle>Resource Usage</CardTitle>
        </div>
          <div className="flex items-center gap-3">
              <p className="mt-1 text-xs text-slate-500">Placeholder values until tai-ui-gateway provides real resource telemetry.</p>
          </div>
        <Tabs value={selectedResource} onValueChange={(value) => setSelectedResource(value as ResourceKind)}>
          <TabsList className="h-9">
            <TabsTrigger value="CPU" className="h-7 px-5">CPU</TabsTrigger>
            <TabsTrigger value="GPU" className="h-7 px-5">GPU</TabsTrigger>
            <TabsTrigger value="RAM" className="h-7 px-5">RAM</TabsTrigger>
          </TabsList>
        </Tabs>
      </CardHeader>
      <CardContent className="grid gap-2 p-4">
        {items.map((item) => {
          const Icon = item.icon;
          return (
            <div key={item.name} className="grid grid-cols-[1.75rem_minmax(8rem,0.22fr)_1fr_3.5rem] items-center gap-3">
              <Icon className="h-5 w-5 text-slate-300" />
              <span className="truncate text-sm font-medium text-slate-200">{item.name}</span>
              <Progress value={item.value} />
              <span className="text-right text-sm font-bold text-sky-300">{item.value}%</span>
            </div>
          );
        })}
      </CardContent>
    </Card>
  );
}
