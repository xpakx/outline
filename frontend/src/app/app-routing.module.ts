import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DisplayComponent } from './component/display/display.component';
import { StartComponent } from './component/start/start.component';

const routes: Routes = [
  { path: '', component: StartComponent },
  { path: ':shortUrl', component: DisplayComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
